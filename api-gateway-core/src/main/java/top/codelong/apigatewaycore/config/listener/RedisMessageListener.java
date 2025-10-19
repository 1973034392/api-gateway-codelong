package top.codelong.apigatewaycore.config.listener;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.GatewayServer;
import top.codelong.apigatewaycore.config.GlobalConfiguration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis消息监听器
 * 用于处理来自Redis的频道消息
 */
@Slf4j
@Component
public class RedisMessageListener implements MessageListener {
    @Resource
    private GlobalConfiguration config;
    @Resource
    private GatewayServer gatewayServer;

    /**
     * 处理Redis频道消息
     * @param message Redis消息对象
     * @param pattern 频道模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        log.debug("收到Redis消息，频道: {}", channel);

        if (channel.equals("heartBeat")) {
            handleHeartbeatMessage();
        } else if (channel.equals("service-launched")) {
            handleServiceLaunchedMessage();
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeatMessage() {
        log.info("处理心跳消息");
        String groupKey = config.getGroupKey();
        String localIp = getLocalIp();
        String localAddr = localIp + ":" + config.getNettyPort();
        String centerAddr = config.getGatewayCenter();
        String fullUrl = centerAddr + "/gateway-group-detail/keep-alive";

        // 构建心跳请求
        HttpRequest request = HttpUtil.createRequest(Method.PUT, fullUrl);
        request.header("Content-Type", "application/json");

        // 设置请求参数
        Map<String, Object> param = new HashMap<>();
        param.put("groupKey", groupKey);
        param.put("addr", localAddr);
        param.put("weight", config.getWeight());
        request.body(JSON.toJSONString(param));

        // 执行请求
        try {
            HttpResponse response = request.execute();
        } catch (Exception e) {
            log.error("心跳维持失败，URL: {}, 错误: {}", fullUrl, e.getMessage());
        }
    }

    /**
     * 处理服务启动消息
     */
    private void handleServiceLaunchedMessage() {
        log.info("处理服务启动消息，更新服务器列表");
        gatewayServer.update();
    }

    /**
     * 获取本地IP地址
     * @return 本地IP地址，失败返回"localhost"
     */
    private String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            log.warn("获取本地IP地址失败，使用localhost代替");
            return "localhost";
        }
    }
}