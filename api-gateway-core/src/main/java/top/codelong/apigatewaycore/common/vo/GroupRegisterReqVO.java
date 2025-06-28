package top.codelong.apigatewaycore.common.vo;

import lombok.Data;


/**
 * 注册请求参数
 */
@Data
public class GroupRegisterReqVO {
    /**
     * 分组key
     */
    private String groupKey;
    /**
     * 实例名称
     */
    private String detailName;
    /**
     * 实例地址
     */
    private String detailAddress;
    /**
     * 实例权重
     */
    private Integer detailWeight;
}
