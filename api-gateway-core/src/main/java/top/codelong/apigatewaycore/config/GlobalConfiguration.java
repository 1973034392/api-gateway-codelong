package top.codelong.apigatewaycore.config;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.service.GenericService;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.vo.GroupDetailRegisterRespVO;
import top.codelong.apigatewaycore.common.vo.GroupRegisterReqVO;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
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
    private CloseableHttpAsyncClient asyncHttpClient;
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
        if (maxCache == null || maxCache <= 0) {
            maxCache = 100; // 再次保障默认值
            log.warn("maxCache未配置，使用默认值: {}", maxCache);
        }
        this.httpStatementMap = CacheUtil.newLFUCache(maxCache);
        log.debug("创建HTTP声明缓存，容量: {}", maxCache);

        this.serverName = register();
        log.info("服务注册完成，服务名称: {}", serverName);

        createAsyncHTTPClient();
        log.info("HTTP客户端初始化完成");
    }

    //在销毁Bean时关闭异步客户端
    @PreDestroy
    public void destroy() {
        try {
            if (asyncHttpClient != null) {
                asyncHttpClient.close();
                log.info("异步HTTP客户端已关闭");
            }
        } catch (IOException e) {
            log.error("关闭HTTP客户端时出错", e);
        }
    }

    /**
     * 创建异步HTTP客户端
     */
    private void createAsyncHTTPClient() {
        log.debug("创建异步HTTP连接池");
        final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(5000, TimeUnit.MILLISECONDS)
                .build();

        final PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20)
                .build();

        this.asyncHttpClient = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .setConnectionManager(connectionManager)
                .build();

        this.asyncHttpClient.start();
        log.debug("异步HTTP连接池创建并启动成功");
    }

    /**
     * 向网关中心注册服务
     *
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

            if(respVO == null){
                throw new Error("服务注册失败");
            }
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