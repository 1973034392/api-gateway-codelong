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
    private Long id;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

