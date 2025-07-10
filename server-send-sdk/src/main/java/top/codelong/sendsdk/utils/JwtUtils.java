package top.codelong.sendsdk.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * JWT工具类
 */
public class JwtUtils {
    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    public static final long TOKEN_EXPIRE_TIME = 30L * 24 * 60 * 60 * 1000;

    public String generateToken(String key, String secret) {
        try {
            Date expireDate = new Date(System.currentTimeMillis() + TOKEN_EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuedAt(new Date())
                    .withExpiresAt(expireDate)
                    .withClaim("safe-key", key)
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("JWT Token生成失败", e);
            throw new RuntimeException("JWT Token生成失败", e);
        }
    }
}