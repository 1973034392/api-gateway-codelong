package top.codelong.apigatewaycore.common.vo;

import lombok.Data;

@Data
public class GroupDetailRegisterRespVO {
    /**
     * 网关服务名称
     */
    private String serverName;
    /**
     * 安全key
     */
    private String safeKey;
    /**
     * 安全密钥
     */
    private String safeSecret;
}
