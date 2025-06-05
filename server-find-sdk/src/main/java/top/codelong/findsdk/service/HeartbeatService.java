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


@Component
public class HeartbeatService {
    @Resource
    private GatewayServerConfig config;
    @Resource
    private Environment environment;

    private final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

    public void heartbeat() {
        String safeKey = config.getSafeKey();
        String serverPort = environment.getProperty("server.port", "8080");
        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        String localAddr = localIp + ":" + serverPort;

        String centerAddr = config.getCenterAddr();
        String fullUrl = centerAddr + "/gateway-server-detail/keep-alive";

        HttpRequest request = HttpUtil.createRequest(Method.PUT, fullUrl);
        request.header("Content-Type", "application/json");
        Map<String, Object> param = new HashMap<>();
        param.put("safeKey", safeKey);
        param.put("addr", localAddr);
        request.body(JSON.toJSONString(param));
        HttpResponse response;
        try {
            response = request.execute();
        } catch (Exception e) {
            logger.error("心跳维持失败: {}", e.getMessage());
            return;
        }
        logger.info("心跳维持成功: {}", response.body());
    }
}
