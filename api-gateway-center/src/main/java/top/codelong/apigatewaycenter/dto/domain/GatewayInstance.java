package top.codelong.apigatewaycenter.dto.domain;

import lombok.Data;

/**
 * 网关实例配置实体对象
 */
@Data
public class GatewayInstance {
    private final String address;
    private int weight;

    public GatewayInstance(String address, int weight) {
        this.address = address;
        this.weight = weight;
    }
}