package top.codelong.findsdk.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.codelong.findsdk.config.GatewayServerConfig;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 心跳服务
 * 负责定期向网关中心发送心跳请求，维持服务活跃状态
 */
@Component
public class HeartbeatService {
    @Resource
    private GatewayServerConfig config;  // 网关服务配置
    @Resource
    private Environment environment;    // 环境配置

    private final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

    /**
     * 发送心跳请求
     * 向网关中心报告当前服务状态
     */
    public void heartbeat() {
        logger.debug("开始发送心跳请求...");

        // 1. 准备心跳参数
        String safeKey = config.getSafeKey();
        String serverPort = environment.getProperty("server.port", "8080");
        String localIp = "localhost";

        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
            logger.debug("获取本地IP地址成功: {}", localIp);
        } catch (Exception e) {
            logger.warn("获取本地IP地址失败，使用默认localhost", e);
        }

        String localAddr = localIp + ":" + serverPort;
        logger.info("当前服务地址: {}", localAddr);

        // 2. 构建请求URL
        String centerAddr = config.getCenterAddr();
        String fullUrl = centerAddr + "/gateway-server-detail/keep-alive";
        logger.debug("网关中心地址: {}", fullUrl);

        // 3. 准备请求参数
        Map<String, Object> param = new HashMap<>();
        param.put("safeKey", safeKey);
        param.put("addr", localAddr);
        logger.trace("心跳请求参数: {}", param);

        // 4. 发送HTTP请求
        HttpRequest request = HttpUtil.createRequest(Method.PUT, fullUrl);
        request.header("Content-Type", "application/json");
        request.body(JSON.toJSONString(param));

        try {
            HttpResponse response = request.execute();
        } catch (Exception e) {
            logger.error("心跳维持失败", e);
        }
    }
}