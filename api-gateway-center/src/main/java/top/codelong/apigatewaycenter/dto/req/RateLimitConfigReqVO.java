package top.codelong.apigatewaycenter.dto.req;

import lombok.Data;

/**
 * 限流配置请求VO
 */
@Data
public class RateLimitConfigReqVO {
    /**
     * 限流规则ID（更新时需要）
     */
    private Long id;

    /**
     * 限流规则名称
     */
    private String ruleName;

    /**
     * 限流类型：GLOBAL、SERVICE、INTERFACE、IP
     */
    private String limitType;

    /**
     * 限流目标
     */
    private String limitTarget;

    /**
     * 限流阈值（每秒请求数）
     */
    private Integer limitCount;

    /**
     * 时间窗口（秒）
     */
    private Integer timeWindow;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 限流策略：TOKEN_BUCKET、SLIDING_WINDOW
     */
    private String strategy;
}

