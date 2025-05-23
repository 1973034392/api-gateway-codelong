package top.codelong.apigatewaycenter.dao.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 方法信息表
 * @TableName gateway_method
 */
@TableName(value ="gateway_method")
@Data
public class GatewayMethodDO {
    /**
     * 唯一id
     */
    @TableId
    private Long id;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型
     */
    private String parameterType;

    /**
     * 方法请求路径
     */
    private String url;

    /**
     * 是否鉴权
     */
    private Integer isAuth;

    /**
     * 是否是HTTP请求
     */
    private Integer isHttp;

    /**
     * HTTP请求类型
     */
    private String httpType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}