package top.codelong.apigatewaycore.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.config.GlobalConfiguration;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 接口缓存工具类
 * 负责从内存缓存或Redis中获取接口配置信息
 */
@Slf4j
@Data
@Component
@AllArgsConstructor
public class InterfaceCacheUtil {
    private final GlobalConfiguration config;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据URL获取接口配置信息
     * @param url 请求URL
     * @return HttpStatement对象，包含接口配置信息
     */
    public HttpStatement getStatement(String url) {
        log.debug("开始获取接口配置，URL: {}", url);

        // 1. 先从内存缓存中查找
        HttpStatement httpStatement = config.getHttpStatementMap().get(url);
        if (httpStatement != null) {
            log.debug("从内存缓存中获取到接口配置");
            return httpStatement;
        }

        log.debug("内存缓存未命中，尝试从Redis获取");
        // 2. 从Redis中获取
        String key = String.format("URL:%s:%s", config.getServerName(), url);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            log.warn("未找到URL对应的接口配置: {}", url);
            return null;
        }

        log.debug("从Redis获取到接口配置，开始构建HttpStatement");
        // 3. 构建HttpStatement对象
        HttpStatement statement = HttpStatement.builder()
                .interfaceName((String) entries.get("interfaceName"))
                .methodName((String) entries.get("methodName"))
                .parameterType(((String) entries.get("parameterType")).split(","))
                .isAuth(entries.get("isAuth").equals(1))
                .isHttp(entries.get("isHttp").equals(1))
                .httpType(HTTPTypeEnum.valueOf((String) entries.get("httpType")))
                .build();

        // 4. 放入内存缓存
        config.getHttpStatementMap().put(url, statement);
        log.debug("接口配置已缓存到内存");

        return statement;
    }
}