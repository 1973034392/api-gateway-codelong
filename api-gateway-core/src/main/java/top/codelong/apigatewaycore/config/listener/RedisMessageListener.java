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
import top.codelong.apigatewaycore.config.GlobalConfiguration;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RedisMessageListener implements MessageListener {
    @Resource
    private GlobalConfiguration config;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        if (channel.equals("heartBeat")) {
            String groupKey = config.getGroupKey();
            String localIp = "localhost";
            try {
                localIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ignored) {
            }
            String localAddr = localIp + ":" + config.getNettyPort();

            String centerAddr = config.getGatewayCenter();
            String fullUrl = centerAddr + "/gateway-group-detail/keep-alive";

            HttpRequest request = HttpUtil.createRequest(Method.PUT, fullUrl);
            request.header("Content-Type", "application/json");

            Map<String, Object> param = new HashMap<>();
            param.put("groupKey", groupKey);
            param.put("addr", localAddr);
            param.put("weight", config.getWeight());
            request.body(JSON.toJSONString(param));
            HttpResponse response;
            try {
                response = request.execute();
            } catch (Exception e) {
                log.error("心跳维持失败: {}", e.getMessage());
                return;
            }
            log.info("心跳维持成功: {}", response.body());
        }
    }
}
