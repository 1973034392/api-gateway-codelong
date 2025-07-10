package top.codelong.sendsdk.common.exception;

/**
 * 网关SDK异常基类
 */
public class GatewayException extends RuntimeException {
    public GatewayException(String message) {
        super(message);
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}