package top.codelong.apigatewaycore.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.config.GlobalConfiguration;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;

import java.util.Map;

@Data
@Component
@AllArgsConstructor
public class InterfaceCacheUtil {
    private final GlobalConfiguration config;
    private final RedisTemplate<String, Object> redisTemplate;

    public HttpStatement getStatement(String url) {
        HttpStatement httpStatement = config.getHttpStatementMap().get(url);
        if (httpStatement != null) {
            return httpStatement;
        }

        String key = String.format("URL:%s:%s", config.getServerName(), url);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return null;
        }
        HttpStatement statement = HttpStatement.builder()
                .interfaceName((String) entries.get("interfaceName"))
                .methodName((String) entries.get("methodName"))
                .parameterType(((String) entries.get("parameterType")).split(","))
                .isAuth(entries.get("isAuth").equals(1))
                .isHttp(entries.get("isHttp").equals(1))
                .httpType(HTTPTypeEnum.valueOf((String) entries.get("httpType")))
                .build();
        config.getHttpStatementMap().put(url, statement);
        return statement;
    }
}
