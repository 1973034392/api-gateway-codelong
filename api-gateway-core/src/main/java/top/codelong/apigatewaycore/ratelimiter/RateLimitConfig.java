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
     * 限流策略
     */
    private String strategy;
}

