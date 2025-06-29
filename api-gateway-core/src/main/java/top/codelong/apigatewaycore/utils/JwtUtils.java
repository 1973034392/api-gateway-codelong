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

/**
 * JWT工具类
 * 提供JWT的生成和验证功能
 */
@Slf4j
@Component
public class JwtUtils {
    // Token过期时间：30天（毫秒）
    public static final long TOKEN_EXPIRE_TIME = 30L * 24 * 60 * 60 * 1000;

    @Resource
    private GlobalConfiguration config;

    /**
     * 生成JWT Token
     * @param key 安全组key，将存储在Token claims中
     * @param value 密钥，用于签名
     * @return 生成的Token字符串，生成失败返回null
     */
    public String sign(String key, String value) {
        log.debug("开始生成JWT Token，安全组key: {}", key);
        try {
            // 设置过期时间
            Date expireDate = new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
            log.trace("Token过期时间设置为: {}", expireDate);

            // 使用密钥创建算法
            Algorithm algorithm = Algorithm.HMAC256(value);

            // 构建Token
            JWTCreator.Builder builder = JWT.create()
                    .withIssuedAt(new Date()) // 发证时间
                    .withExpiresAt(expireDate) // 过期时间
                    .withClaim("safe-key", key); // 自定义claim

            String token = builder.sign(algorithm);
            log.debug("JWT Token生成成功");
            return token;
        } catch (Exception e) {
            log.error("JWT Token生成失败", e);
            return null;
        }
    }

    /**
     * 验证JWT Token是否有效
     * @param token 待验证的Token字符串
     * @return true-验证通过 false-验证失败
     */
    public boolean verify(String token) {
        log.debug("开始验证JWT Token");
        if (StrUtil.isBlank(token)) {
            log.warn("Token为空，验证失败");
            return false;
        }

        try {
            // 使用配置的密钥创建验证器
            Algorithm algorithm = Algorithm.HMAC256(config.getSafeSecret());
            JWTVerifier verifier = JWT.require(algorithm).build();

            // 验证Token并获取claim
            Claim claim = verifier.verify(token).getClaims().get("safe-key");
            boolean isValid = claim.asString().equals(config.getSafeKey());

            if (isValid) {
                log.debug("JWT Token验证通过");
            } else {
                log.warn("JWT Token验证失败：安全组key不匹配");
            }
            return isValid;
        } catch (Exception e) {
            log.error("JWT Token验证异常", e);
            return false;
        }
    }
}