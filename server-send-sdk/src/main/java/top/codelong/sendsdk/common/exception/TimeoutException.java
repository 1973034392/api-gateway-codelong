package top.codelong.sendsdk.common.exception;

/**
 * 请求超时异常
 */
public class TimeoutException extends GatewayException {
    public TimeoutException(String message) {
        super(message);
    }
}