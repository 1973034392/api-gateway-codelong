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
    }

    /**
     * 添加实例
     *
     * @param address 网关地址（host:port）
     * @param weight  权重
     */
    public void addInstance(String address, int weight) {
        GatewayInstance instance = new GatewayInstance(address, weight);
        instances.put(address, instance);
        refreshNginxConfig();
    }

    /**
     * 删除实例
     *
     * @param address 网关地址（host:port）
     */
    public void removeInstance(String address) {
        if (instances.remove(address) != null) {
            refreshNginxConfig();
        }
    }

    /**
     * 更新实例权重
     *
     * @param address   网关地址
     * @param newWeight 新权重
     */
    public void updateInstanceWeight(String address, int newWeight) {
        GatewayInstance instance = instances.get(address);
        if (instance != null && instance.getWeight() != newWeight) {
            instance.setWeight(newWeight);
            refreshNginxConfig();
        }
    }

    /**
     * 刷新NGINX配置
     */
    public void refreshNginxConfig() {
        if (!refreshLock.tryLock()) {
            // 防止并发刷新
            return;
        }

        try {
            String config = generateNginxConfig();
            uploadConfigToRemote(config);
            reloadNginxOnRemote();
        } finally {
            refreshLock.unlock();
        }
    }

    /**
     * 生成NGINX负载均衡配置
     */
    private String generateNginxConfig() {
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
     *
     * @param configContent 配置内容
     */
    private void uploadConfigToRemote(String configContent) {
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
            }

        } catch (JSchException | SftpException | java.io.IOException e) {
            throw new RuntimeException("NGINX配置上传失败: " + e.getMessage(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    /**
     * 远程重载NGINX
     */
    private void reloadNginxOnRemote() {
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
            if (channel.getExitStatus() != 0) {
                log.warn("NGINX退出码为: " + channel.getExitStatus());
            }

        } catch (JSchException | InterruptedException e) {
            throw new RuntimeException("NGINX重载失败: " + e.getMessage(), e);
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}
