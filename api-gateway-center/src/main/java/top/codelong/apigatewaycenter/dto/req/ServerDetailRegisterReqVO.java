package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "网关服务详情注册请求参数")
public class ServerDetailRegisterReqVO {
    @Schema(description = "服务id")
    private Long serverId;
    @Schema(description = "服务地址")
    private String serverAddress;
}
