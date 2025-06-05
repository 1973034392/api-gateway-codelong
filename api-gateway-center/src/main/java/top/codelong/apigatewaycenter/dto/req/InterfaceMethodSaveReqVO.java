package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import top.codelong.apigatewaycenter.dto.domain.MethodSaveDomain;

import java.util.List;

@Data
@Schema(description = "接口和方法 ReqVO")
public class InterfaceMethodSaveReqVO {
    @Schema(description = "服务URL")
    private String serverUrl;
    @Schema(description = "安全key")
    private String safeKey;
    @Schema(description = "安全密钥")
    private String safeSecret;
    @Schema(description = "接口名称")
    private String interfaceName;
    @Schema(description = "接口方法信息")
    private List<MethodSaveDomain> methods;
}
