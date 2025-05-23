package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.codelong.apigatewaycenter.common.page.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "网关实例组详情分页请求参数")
public class GroupDetailPageReqVO extends PageParam {
    @Schema(description = "网关实例组名称")
    private String groupName;
    @Schema(description = "网关实例名称")
    private String detailName;
    @Schema(description = "网关实例状态")
    private Integer status;
    @Schema(description = "网关实例地址")
    private String address;
}
