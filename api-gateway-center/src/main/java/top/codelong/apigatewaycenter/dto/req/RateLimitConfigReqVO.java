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

    /**
     * 限流模式：DISTRIBUTED(分布式)、LOCAL_DISTRIBUTED(本地+分布式混合)
     * 默认为 DISTRIBUTED
     */
    private String mode;

    /**
     * 本地限流批量获取令牌数（仅在 LOCAL_DISTRIBUTED 模式下使用）
     * 从 Redis 批量获取的令牌数，默认为 100
     */
    private Integer localBatchSize;

    /**
     * 本地限流器容量倍数（仅在 LOCAL_DISTRIBUTED 模式下使用）
     * 用于计算本地限流器的容量，默认为 1.0
     */
    private Double localCapacityMultiplier;
}

