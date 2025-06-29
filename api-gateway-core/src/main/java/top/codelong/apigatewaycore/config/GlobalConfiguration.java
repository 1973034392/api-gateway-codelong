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
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.vo.GroupDetailRegisterRespVO;
import top.codelong.apigatewaycore.common.vo.GroupRegisterReqVO;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private Cache<String, HttpStatement> httpStatementMap;
    private CloseableHttpClient httpClient;
    private ConcurrentHashMap<String, GenericService> dubboServiceMap = new ConcurrentHashMap<>();

    @Resource
    private Environment environment;

    @PostConstruct
    public void init() {
        this.httpStatementMap = CacheUtil.newLRUCache(maxCache);
        this.serverName = register();
        createHTTPClient();
    }

    private void createHTTPClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(500);
        cm.setDefaultMaxPerRoute(50);

        // 增加空闲连接检查
        cm.setValidateAfterInactivity(30_000);

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();

        // 添加连接回收线程
        startConnectionEvictor(cm);
    }

    private void startConnectionEvictor(PoolingHttpClientConnectionManager cm) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            cm.closeExpiredConnections();
            cm.closeIdleConnections(30, TimeUnit.SECONDS);
        }, 30, 30, TimeUnit.SECONDS);
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