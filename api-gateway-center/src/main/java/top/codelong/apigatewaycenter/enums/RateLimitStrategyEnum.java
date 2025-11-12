package top.codelong.apigatewaycenter.enums;

/**
 * 限流策略枚举
 */
public enum RateLimitStrategyEnum {
    /**
     * 令牌桶算法
     */
    TOKEN_BUCKET,

    /**
     * 滑动窗口算法
     */
    SLIDING_WINDOW
}

