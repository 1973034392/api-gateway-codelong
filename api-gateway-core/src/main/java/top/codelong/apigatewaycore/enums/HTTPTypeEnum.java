package top.codelong.apigatewaycore.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HTTP请求类型枚举
 */
@Getter
@AllArgsConstructor
public enum HTTPTypeEnum {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String value;
}
