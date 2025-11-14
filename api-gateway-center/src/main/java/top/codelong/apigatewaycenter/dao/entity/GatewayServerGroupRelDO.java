package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关系统和实例分组关联表
 * @TableName gateway_server_group_rel
 */
@TableName(value ="gateway_server_group_rel")
@Data
public class GatewayServerGroupRelDO {
    /**
     * 唯一id
     */
    @TableId
    private String id;

    /**
     * 网关服务唯一id
     */
    private String serverId;

    /**
     * 网关系统分组唯一id
     */
    private String groupId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}