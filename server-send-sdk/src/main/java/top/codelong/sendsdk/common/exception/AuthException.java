package top.codelong.sendsdk.common.exception;

/**
 * 认证异常
 */
public class AuthException extends GatewayException {
    public AuthException(String message) {
        super(message);
    }
}