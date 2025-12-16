package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关限流配置表
 * @TableName gateway_rate_limit
 */
@TableName(value = "gateway_rate_limit")
@Data
public class GatewayRateLimitDO {
    /**
     * 唯一id
     */
    @TableId
    private String id;

    /**
     * 限流规则名称
     */
    private String ruleName;

    /**
     * 限流类型：GLOBAL(全局)、SERVICE(服务级)、INTERFACE(接口级)、IP(IP级)
     */
    private String limitType;

    /**
     * 限流目标：服务名、接口URL、IP地址等
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
     * 限流策略：TOKEN_BUCKET(令牌桶)、SLIDING_WINDOW(滑动窗口)
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

