package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "网关服务保存参数")
public class ServerSaveReqVO {
    @Schema(description = "服务id")
    private Long id;
    @Schema(description = "服务名称")
    private String serverName;
    @Schema(description = "状态")
    private Integer status;
    @Schema(description = "安全key")
    private String safeKey;
    @Schema(description = "安全密钥")
    private String safeSecret;
}
