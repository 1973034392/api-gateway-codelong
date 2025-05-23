package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import top.codelong.apigatewaycenter.common.page.PageParam;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "网关实例组分页请求参数")
public class GroupPageReqVO extends PageParam {
    @Schema(description = "网关实例组名称")
    private String name;
}
