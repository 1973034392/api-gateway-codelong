package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    private Long id;
}