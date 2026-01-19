package top.codelong.apigatewaycenter.utils;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.config.NginxConfig;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Nginx配置工具类
 * based on Redis heartbeat data
 */
@Slf4j
@Component
public class NginxConfUtil {

    private final ReentrantLock refreshLock = new ReentrantLock();
    private final NginxConfig properties;
    private final StringRedisTemplate redisTemplate;

    // 用于缓存上一次成功应用的配置内容
    private volatile String lastAppliedConfig = null;

    // Redis Key 前缀定义
    private static final String HEARTBEAT_KEY_PATTERN = "heartbeat:group:*:*";

    @Autowired
    public NginxConfUtil(NginxConfig properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        log.info("NginxConfUtil初始化完成，配置: {}", properties);
    }

    /**
     * 刷新Nginx配置
     * 变更点：增加配置比对，只有内容变化时才执行上传和重载
     */
    public void refreshNginxConfig() {
        log.debug("开始检查Nginx配置是否需要刷新");
        if (!refreshLock.tryLock()) {
            log.warn("Nginx配置正在被其他线程刷新，本次跳过");
            return;
        }

        try {
            // 1. 从Redis获取数据并生成配置
            String newConfig = generateNginxConfigFromRedis();

            // 配置比对逻辑
            if (lastAppliedConfig != null && lastAppliedConfig.equals(newConfig)) {
                log.info("检测到Nginx配置内容未发生变更，跳过刷新操作");
                return;
            }

            log.debug("检测到配置变更，准备更新。新配置预览:\n{}", newConfig);

            // 2. 上传配置
            uploadConfigToRemote(newConfig);

            // 3. 重载Nginx
            reloadNginxOnRemote();

            // 只有在上传和重载都无异常后，才更新本地缓存
            lastAppliedConfig = newConfig;

            log.info("Nginx配置刷新并重载完成");
        } catch (Exception e) {
            log.error("刷新Nginx配置过程中发生异常", e);
            // 注意：发生异常时不更新 lastAppliedConfig，确保下次重试
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * 基于Redis数据生成Nginx配置
     */
    private String generateNginxConfigFromRedis() {
        // 获取所有符合模式的Key
        Set<String> keys = redisTemplate.keys(HEARTBEAT_KEY_PATTERN);

        // 存储结构: Map<服务名, List<地址>>
        // 使用 TreeMap 保证服务名排序，防止因为Map无序导致生成的字符串顺序不同，从而误判配置发生变化
        Map<String, Set<String>> serviceMap = new TreeMap<>();

        if (!keys.isEmpty()) {
            for (String key : keys) {
                try {
                    // key格式: heartbeat:server:test-server:192.168.1.5:8080
                    String[] parts = key.split(":");
                    if (parts.length >= 4) {
                        String serverName = parts[2];
                        String address = Arrays.stream(parts, 3, parts.length)
                                .collect(Collectors.joining(":"));

                        // 使用 TreeSet 保证后端IP排序，同样为了防止误判配置变化
                        serviceMap.computeIfAbsent(serverName, k -> new TreeSet<>()).add(address);
                    }
                } catch (Exception e) {
                    log.warn("解析Redis Key出错: {}", key, e);
                }
            }
        }

        StringBuilder builder = new StringBuilder();

        // --- 头部配置 ---
        builder.append("events {\n")
                .append("    worker_connections 1024;\n")
                .append("}\n\n");

        builder.append("http {\n\n");

        // --- 1. 生成 Upstream 块 ---
        for (Map.Entry<String, Set<String>> entry : serviceMap.entrySet()) {
            String serverName = entry.getKey();
            Set<String> addresses = entry.getValue();

            builder.append(String.format("    upstream %s {\n", serverName));
            for (String addr : addresses) {
                builder.append(String.format("        server %s weight=1;\n", addr));
            }
            builder.append("    }\n\n");
        }

        // --- 2. 生成 Server 和 Location 块 ---
        builder.append("    server {\n");
        builder.append("        listen 80;\n\n");

        for (String serverName : serviceMap.keySet()) {
            builder.append(String.format("        location /%s/ {\n", serverName));

            // 优化：使用 rewrite 方式，这是处理路径截取最稳健的方式
            // 解决之前提到的 /test-server/test2 -> /test2 的问题
            builder.append(String.format("            rewrite ^/%s/(.*)$ /$1 break;\n", serverName));

            // 注意：使用 rewrite 后，proxy_pass 后面不需要加 /
            builder.append(String.format("            proxy_pass http://%s;\n", serverName));

            builder.append("            proxy_set_header Host $host;\n");
            builder.append("            proxy_set_header X-Real-IP $remote_addr;\n");
            builder.append("        }\n\n");
        }

        // 默认兜底 location
        builder.append("        location / {\n");
        builder.append("            return 404;\n");
        builder.append("        }\n");

        builder.append("    }\n"); // end server
        builder.append("}\n"); // end http

        return builder.toString();
    }

    /**
     * 上传配置到远程服务器
     */
    private void uploadConfigToRemote(String configContent) {
        log.info("开始上传Nginx配置到远程服务器");
        Session session = null;
        ChannelSftp channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());
            session.setPassword(properties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            // 上传配置文件
            try (OutputStream out = channel.put(properties.getConfigPath())) {
                byte[] contentBytes = configContent.getBytes();
                out.write(contentBytes);
                log.info("Nginx配置成功上传到: {}", properties.getConfigPath());
            }

        } catch (JSchException | SftpException | java.io.IOException e) {
            log.error("NGINX配置上传失败", e);
            throw new RuntimeException("NGINX配置上传失败: " + e.getMessage(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    /**
     * 远程重载Nginx服务
     */
    private void reloadNginxOnRemote() {
        log.info("开始远程重载Nginx服务");
        Session session = null;
        ChannelExec channel = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(properties.getUsername(), properties.getHost(), properties.getPort());
            session.setPassword(properties.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(properties.getReloadCommand());
            channel.connect();

            // 等待命令执行完成
            while (!channel.isClosed()) {
                Thread.sleep(500);
            }

            // 检查退出状态
            int exitStatus = channel.getExitStatus();
            if (exitStatus != 0) {
                log.warn("NGINX重载命令返回非零状态码: {}", exitStatus);
            } else {
                log.info("Nginx重载成功");
            }

        } catch (JSchException | InterruptedException e) {
            log.error("NGINX重载失败", e);
            throw new RuntimeException("NGINX重载失败: " + e.getMessage(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}