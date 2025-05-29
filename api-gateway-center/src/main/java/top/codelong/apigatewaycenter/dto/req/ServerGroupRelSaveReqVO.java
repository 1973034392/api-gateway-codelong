package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "网关服务实例组关联创建 ReqVO")
public class ServerGroupRelSaveReqVO {
    @Schema(description = "网关服务id")
    private Long serverId;
    @Schema(description = "网关分组id")
    private Long groupId;
}
