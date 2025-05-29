package top.codelong.apigatewaycenter.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口方法信息")
public class MethodSaveDomain {
    @Schema(description = "方法名称")
    private String methodName;
    @Schema(description = "参数类型")
    private String parameterType;
    @Schema(description = "方法路径")
    private String url;
    @Schema(description = "是否需要认证")
    private Integer isAuth;
    @Schema(description = "是否是http请求")
    private Integer isHttp;
    @Schema(description = "http请求类型")
    private String httpType;
}
