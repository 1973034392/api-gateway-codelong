package top.codelong.findsdk.enums;

public enum HttpTypeEnum {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String value;

    HttpTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}