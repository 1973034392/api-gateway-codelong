package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "网关实例组详情请求参数")
public class GroupDetailSaveReqVO {
    @Schema(description = "网关实例组详情id")
    private Long id;
    @Schema(description = "网关实例组key")
    private String groupKey;
    @Schema(description = "网关实例详情名称")
    private String name;
    @Schema(description = "网关实例详情地址")
    private String address;
    @Schema(description = "网关实例详情状态")
    private Integer status;
    @Schema(description = "网关实例详情权重")
    private Integer weight;
}
