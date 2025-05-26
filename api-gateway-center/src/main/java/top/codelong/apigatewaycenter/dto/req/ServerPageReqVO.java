package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.codelong.apigatewaycenter.common.page.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "网关服务分页请求参数")
public class ServerPageReqVO extends PageParam {
    @Schema(description = "网关服务名称")
    private String serverName;
    @Schema(description = "网关服务状态")
    private Integer status;
    @Schema(description = "网关服务地址")
    private String nginxAddr;
}
