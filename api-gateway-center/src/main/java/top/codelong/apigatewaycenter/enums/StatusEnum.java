package top.codelong.apigatewaycenter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态枚举
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    ENABLE(1, "启用"),
    DISABLE(0, "禁用");
    private final Integer value;
    private final String message;
}
