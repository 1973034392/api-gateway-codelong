package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关系统表
 *
 * @TableName gateway_server
 */
@TableName(value = "gateway_server")
@Data
public class GatewayServerDO {
    /**
     * 唯一id
     */
    @TableId
    private String id;

    /**
     * 服务名
     */
    private String serverName;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 安全组唯一标识
     */
    private String safeKey;

    /**
     * 安全组秘钥
     */
    private String safeSecret;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}