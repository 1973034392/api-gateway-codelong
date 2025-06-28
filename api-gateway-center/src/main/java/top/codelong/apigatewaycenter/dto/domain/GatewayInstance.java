package top.codelong.apigatewaycenter.dto.domain;

import lombok.Data;

@Data
public class GatewayInstance {
    private final String address;
    private int weight;

    public GatewayInstance(String address, int weight) {
        this.address = address;
        this.weight = weight;
    }
}