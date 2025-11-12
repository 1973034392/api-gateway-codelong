package top.codelong.apigatewaycenter.enums;

/**
 * 限流类型枚举
 */
public enum RateLimitTypeEnum {
    /**
     * 全局限流
     */
    GLOBAL,

    /**
     * 服务级限流
     */
    SERVICE,

    /**
     * 接口级限流
     */
    INTERFACE,

    /**
     * IP级限流
     */
    IP
}

