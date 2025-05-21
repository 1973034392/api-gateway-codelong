package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 网关实例分组表
 * @TableName gateway_group
 */
@TableName(value ="gateway_group")
@Data
public class GatewayGroupDO {
    /**
     * 唯一id
     */
    @TableId
    private Long id;
}