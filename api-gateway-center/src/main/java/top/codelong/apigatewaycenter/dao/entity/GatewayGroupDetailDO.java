package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网关实例信息表
 * @TableName gateway_group_detail
 */
@TableName(value ="gateway_group_detail")
@Data
public class GatewayGroupDetailDO {
    /**
     * 唯一id
     */
    @TableId
    private String id;

    /**
     * 分组唯一标识id
     */
    private String groupId;

    /**
     * 网关实例名称
     */
    private String detailName;

    /**
     * 网关实例地址
     */
    private String detailAddress;

    /**
     * 网关实例启用状态
     */
    private Integer status;

    /**
     * 网关实例分配权重
     */
    private Integer detailWeight;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}