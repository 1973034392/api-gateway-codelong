package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注册请求参数")
public class GroupRegisterReqVO {
    @Schema(description = "分组key")
    private String groupKey;
    @Schema(description = "实例名称")
    private String detailName;
    @Schema(description = "实例地址")
    private String detailAddress;
    @Schema(description = "实例权重")
    private Integer detailWeight;
}
