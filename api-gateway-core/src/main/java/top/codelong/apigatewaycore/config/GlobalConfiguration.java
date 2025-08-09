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

/**
 * 全局配置类
 * 负责管理网关的核心配置和初始化工作
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "api-gateway")
public class GlobalConfiguration {
    /**
     * Netty服务端口
     */
    private Integer nettyPort;
    /**
     * 网关中心地址
     */
    private String gatewayCenter;
    /**
     * 分组Key
     */
    private String groupKey;
    /**
     * 服务权重
     */
    private Integer weight;
    /**
     * 最大缓存数量
     */
    private Integer maxCache;
    /**
     * Netty boss线程数
     */
    private Integer bossThreads;
    /**
     * Netty worker线程数
     */
    private Integer workerThreads;
    /**
     * 服务名称(注册后获取)
     */
    private String serverName;
    /**
     * 安全Key(注册后获取)
     */
    private String safeKey;
    /**
     * 安全密钥(注册后获取)
     */
    private String safeSecret;
    /**
     * HTTP请求声明缓存
     */
    private Cache<String, HttpStatement> httpStatementMap;
    /**
     * HTTP客户端
     */
    private CloseableHttpClient httpClient;
    /**
     * Dubbo服务缓存
     */
    private ConcurrentHashMap<String, GenericService> dubboServiceMap = new ConcurrentHashMap<>();

    @Resource
    private Environment environment;

    /**
     * 初始化方法
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化全局配置");
        this.httpStatementMap = CacheUtil.newLFUCache(maxCache);
        log.debug("创建HTTP声明缓存，容量: {}", maxCache);

        this.serverName = register();
        log.info("服务注册完成，服务名称: {}", serverName);

        createHTTPClient();
        log.info("HTTP客户端初始化完成");
    }

    /**
     * 创建HTTP客户端连接池
     */
    private void createHTTPClient() {
        log.debug("创建HTTP连接池");
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(500);
        cm.setDefaultMaxPerRoute(50);
        cm.setValidateAfterInactivity(30_000);

        httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        log.debug("HTTP连接池创建成功，最大连接数: 500，单路由最大连接数: 50");

        startConnectionEvictor(cm);
    }

    /**
     * 启动连接回收线程
     * @param cm 连接管理器
     */
    private void startConnectionEvictor(PoolingHttpClientConnectionManager cm) {
        log.debug("启动HTTP连接回收线程");
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            cm.closeExpiredConnections();
            cm.closeIdleConnections(30, TimeUnit.SECONDS);
            log.trace("HTTP连接池清理完成");
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 向网关中心注册服务
     * @return 注册成功的服务名称
     * @throws Error 当注册失败时抛出
     */
    private String register() {
        log.info("开始向网关中心注册服务");
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
            log.debug("获取本地IP地址: {}", localIp);
        } catch (Exception e) {
            log.warn("获取本地IP失败，使用默认localhost");
        }
        registerVO.setDetailAddress(localIp + ":" + nettyPort);
        registerVO.setDetailWeight(this.weight);

        log.debug("注册请求参数: {}", registerVO);
        request.body(JSON.toJSONString(registerVO));

        try {
            HttpResponse response = request.execute();
            String body = response.body();
            log.debug("注册响应: {}", body);

            String result = JSON.parseObject(body).getString("data");
            GroupDetailRegisterRespVO respVO = JSON.parseObject(result, GroupDetailRegisterRespVO.class);

            if (StrUtil.isNotBlank(result)) {
                log.info("服务注册成功，服务名称: {}", respVO.getServerName());
            }

            this.safeKey = respVO.getSafeKey();
            this.safeSecret = respVO.getSafeSecret();
            log.debug("获取安全凭证: key={}", safeKey);

            return respVO.getServerName();
        } catch (Exception e) {
            log.error("服务注册失败，URL: {}, 错误: {}", fullUrl, e.getMessage());
            throw new Error("服务注册失败");
        }
    }
}