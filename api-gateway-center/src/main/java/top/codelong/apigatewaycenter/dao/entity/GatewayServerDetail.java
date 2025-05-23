package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 系统详细信息表
 * @TableName gateway_server_detail
 */
@TableName(value ="gateway_server_detail")
@Data
public class GatewayServerDetail {
    /**
     * 唯一id
     */
    @TableId
    private Long id;

    /**
     * 系统唯一标识id
     */
    private Long serverId;

    /**
     * 系统实例地址
     */
    private String serverAddress;

    /**
     * 系统实例启用状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}