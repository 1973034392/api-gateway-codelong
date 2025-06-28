package top.codelong.apigatewaycore.utils;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycore.config.GlobalConfiguration;

import java.util.Date;

@Slf4j
@Component
public class JwtUtils {
    //过期时间 三十天
    public static final long TOKEN_EXPIRE_TIME = 30L * 24 * 60 * 60 * 1000;

    @Resource
    private GlobalConfiguration config;

    /**
     * 生成签名
     *
     * @param key   安全组key
     * @param value 密钥
     * @return Token
     */
    public String sign(String key, String value) {
        try {
            // 设置过期时间
            Date date = new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
            // 私钥和加密算法
            Algorithm algorithm = Algorithm.HMAC256(value);
            // 返回token字符串
            JWTCreator.Builder builder = JWT.create()
                    .withIssuedAt(new Date()) //发证时间
                    .withExpiresAt(date);  //过期时间
            builder.withClaim("safe-key", key);

            return builder.sign(algorithm);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 检验token是否正确
     *
     * @param **token**
     * @return
     */
    public boolean verify(String token) {
        if (StrUtil.isBlank(token)) {
            return false;
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(config.getSafeSecret());
            JWTVerifier verifier = JWT.require(algorithm).build();
            Claim claim = verifier.verify(token).getClaims().get("safe-key");

            return claim.asString().equals(config.getSafeKey());
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
