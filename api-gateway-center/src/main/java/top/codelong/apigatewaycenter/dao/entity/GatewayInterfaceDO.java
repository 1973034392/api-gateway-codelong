package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口信息表
 * @TableName gateway_interface
 */
@TableName(value ="gateway_interface")
@Data
public class GatewayInterfaceDO {
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
     * 接口名
     */
    private String interfaceName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}