package top.codelong.apigatewaycenter.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "网关实例组详情注册响应")
public class GroupDetailRegisterRespVO {
    @Schema(description = "网关服务名称")
    private String serverName;
    @Schema(description = "安全key")
    private String safeKey;
    @Schema(description = "安全密钥")
    private String safeSecret;
}
