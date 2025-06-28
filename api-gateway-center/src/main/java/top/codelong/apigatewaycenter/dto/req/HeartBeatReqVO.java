package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "心跳请求参数 ReqVO")
public class HeartBeatReqVO {
    private String safeKey;
    private String groupKey;
    private String server;
    private String addr;
    private Integer weight;
}
