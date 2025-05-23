package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 网关实例分组表
 * @TableName gateway_group
 */
@TableName(value ="gateway_group")
@Data
public class GatewayGroupDO {
    /**
     * 唯一标识id
     */
    @TableId
    private Long id;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组唯一标识
     */
    private String groupKey;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}