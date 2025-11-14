package top.codelong.apigatewaycenter.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "网关服务实例组关联响应VO")
public class ServerGroupRelRespVO {
    @Schema(description = "关联ID")
    private String id;

    @Schema(description = "核心服务ID")
    private String serverId;

    @Schema(description = "核心服务名称")
    private String serverName;

    @Schema(description = "核心服务Key")
    private String serverKey;

    @Schema(description = "网关服务组ID")
    private String groupId;

    @Schema(description = "网关服务组名称")
    private String groupName;

    @Schema(description = "网关服务组Key")
    private String groupKey;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

