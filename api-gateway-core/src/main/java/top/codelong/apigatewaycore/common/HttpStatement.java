package top.codelong.apigatewaycore.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;

/**
 * http请求信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpStatement {
    private String interfaceName;
    private String methodName;
    private String[] parameterType;
    private Boolean isAuth;
    private Boolean isHttp;
    private HTTPTypeEnum httpType;
    private String serviceId;

    public String getServiceId() {
        // 如果 serviceId 为空，使用接口名作为服务ID
        return serviceId != null ? serviceId : interfaceName;
    }
}
