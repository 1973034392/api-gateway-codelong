package top.codelong.apigatewaycore.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;

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
}
