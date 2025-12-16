package top.codelong.apigatewaycore.ratelimiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 限流配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitConfig {
    /**
     * 限流规则ID
     */
    private Long id;

    /**
     * 限流规则名称
     */
    private String ruleName;

    /**
     * 限流类型
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
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 限流策略：TOKEN_BUCKET 或 SLIDING_WINDOW
     */
    private String strategy;

    /**
     * 限流模式：DISTRIBUTED（分布式）、LOCAL_DISTRIBUTED（本地+分布式混合）
     * 默认为 DISTRIBUTED
     */
    @Builder.Default
    private String mode = "DISTRIBUTED";

    /**
     * 本地限流批量获取令牌数（仅在 LOCAL_DISTRIBUTED 模式下使用）
     * 从 Redis 批量获取的令牌数，然后在本地进行限流
     */
    private Integer localBatchSize;

    /**
     * 本地限流器容量倍数（仅在 LOCAL_DISTRIBUTED 模式下使用）
     * 用于计算本地限流器的容量：limitCount * localCapacityMultiplier
     * 默认为 1.0（精确限流）
     */
    @Builder.Default
    private Double localCapacityMultiplier = 1.0;
}

