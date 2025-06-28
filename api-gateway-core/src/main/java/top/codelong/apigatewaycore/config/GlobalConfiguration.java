package top.codelong.apigatewaycore.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.vo.GroupDetailRegisterRespVO;
import top.codelong.apigatewaycore.common.vo.GroupRegisterReqVO;
import top.codelong.apigatewaycore.connection.ConnectionResourcePool;

import java.net.InetAddress;

@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "api-gateway")
public class GlobalConfiguration {
    /**
     * 配置文件中获取
     */
    private Integer nettyPort;
    private String gatewayCenter;
    private String groupKey;
    private Integer weight;
    private Integer maxCache;
    private Integer bossThreads;
    private Integer workerThreads;
    /**
     * 自定义系统配置
     */
    private String serverName;
    private String safeKey;
    private String safeSecret;
    private ConnectionResourcePool<String> connectionPool;
    private Cache<String, HttpStatement> httpStatementMap;

    @Resource
    private Environment environment;

    @PostConstruct
    public void init() {
        this.httpStatementMap = CacheUtil.newLRUCache(1000, 3600 * 1000L);
        this.connectionPool = new ConnectionResourcePool<>(maxCache);
        this.serverName = register();
    }

    /**
     * 向网关中心注册服务
     *
     * @return 服务名称
     */
    private String register() {
        String addr = this.gatewayCenter;
        String fullUrl = addr + "/gateway-group-detail/register";

        HttpRequest request = HttpUtil.createRequest(cn.hutool.http.Method.POST, fullUrl);
        request.header("Content-Type", "application/json");

        GroupRegisterReqVO registerVO = new GroupRegisterReqVO();
        registerVO.setGroupKey(this.groupKey);
        registerVO.setDetailName(environment.getProperty("spring.application.name", "api-gateway-core"));

        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        registerVO.setDetailAddress(localIp + ":" + nettyPort);
        registerVO.setDetailWeight(this.weight);

        request.body(JSON.toJSONString(registerVO));
        try {
            HttpResponse response = request.execute();
            String body = response.body();
            String result = JSON.parseObject(body).getString("data");
            GroupDetailRegisterRespVO respVO = JSON.parseObject(result, GroupDetailRegisterRespVO.class);
            if (StrUtil.isNotBlank(result)) {
                log.info("服务注册成功: {}", respVO.getServerName());
            }
            this.safeKey = respVO.getSafeKey();
            this.safeSecret = respVO.getSafeSecret();
            return respVO.getServerName();
        } catch (Exception e) {
            throw new Error("服务注册失败");
        }
    }
}