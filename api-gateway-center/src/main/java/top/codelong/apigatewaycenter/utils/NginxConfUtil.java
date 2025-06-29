package top.codelong.apigatewaycenter.utils;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.config.NginxConfig;
import top.codelong.apigatewaycenter.dto.domain.GatewayInstance;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Nginx配置工具类
 * 用于管理网关实例并动态更新Nginx配置
 */
@Slf4j
@Component
public class NginxConfUtil {
    // 使用线程安全的集合存储实例
    private final Map<String, GatewayInstance> instances = new ConcurrentHashMap<>();
    private final ReentrantLock refreshLock = new ReentrantLock();

    private final NginxConfig properties;

    @Autowired
    public NginxConfUtil(NginxConfig properties) {
        this.properties = properties;
        log.info("NginxConfUtil初始化完成，配置: {}", properties);
    }

    /**
     * 添加网关实例
     * @param address 网关地址（host:port）
     * @param weight 权重值
     */
    public void addInstance(String address, int weight) {
        log.info("添加网关实例，address: {}, weight: {}", address, weight);
        GatewayInstance instance = new GatewayInstance(address, weight);
        instances.put(address, instance);
        refreshNginxConfig();
    }

    /**
     * 删除网关实例
     * @param address 要删除的网关地址
     */
    public void removeInstance(String address) {
        log.info("删除网关实例，address: {}", address);
        if (instances.remove(address) != null) {
            refreshNginxConfig();
        } else {
            log.warn("要删除的网关实例不存在，address: {}", address);
        }
    }

    /**
     * 更新实例权重
     * @param address 网关地址
     * @param newWeight 新的权重值
     */
    public void updateInstanceWeight(String address, int newWeight) {
        log.info("更新实例权重，address: {}, newWeight: {}", address, newWeight);
        GatewayInstance instance = instances.get(address);
        if (instance != null && instance.getWeight() != newWeight) {
            instance.setWeight(newWeight);
            refreshNginxConfig();
        } else {
            log.warn("实例不存在或权重未变化，address: {}", address);
        }
    }

    /**
     * 刷新Nginx配置
     */
    public void refreshNginxConfig() {
        log.debug("开始刷新Nginx配置");
        if (!refreshLock.tryLock()) {
            log.warn("Nginx配置正在被其他线程刷新，本次跳过");
            return;
        }

        try {
            String config = generateNginxConfig();
            log.debug("生成的Nginx配置:\n{}", config);
            uploadConfigToRemote(config);
            reloadNginxOnRemote();
            log.info("Nginx配置刷新完成");
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * 生成Nginx负载均衡配置
     * @return 生成的配置内容
     */
    private String generateNginxConfig() {
        log.debug("生成Nginx配置，当前实例数: {}", instances.size());
        StringBuilder builder = new StringBuilder();
        builder.append("events {\n" +
                "    worker_connections 1024;\n" +
                "}\n\n");
        builder.append("http {\n\n");
        builder.append("upstream gateway_backend {\n");

        // 添加实例配置
        for (GatewayInstance instance : instances.values()) {
            String[] parts = instance.getAddress().split(":");
            String server = parts[0];
            String port = parts.length > 1 ? parts[1] : "80";

            builder.append(String.format(
                    "    server %s:%s weight=%d;\n",
                    server, port, instance.getWeight()
            ));
        }

        builder.append("}\n\n");

        // 代理配置
        builder.append("    server {\n");
        builder.append("        listen 80;\n");
        builder.append("        location / {\n");
        builder.append("            proxy_pass http://gateway_backend;\n");
        builder.append("            proxy_set_header Host $host;\n");
        builder.append("            proxy_set_header X-Real-IP $remote_addr;\n");
        builder.append("        }\n");
        builder.append("    }\n");
        builder.append("}\n");

        return builder.toString();
    }

    /**
     * 上传配置到远程服务器
     * @param configContent 配置内容
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