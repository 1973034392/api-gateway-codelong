package top.codelong.apigatewaycenter.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "网关实例组保存请求参数")
public class GroupSaveReqVO {
    @Schema(description = "网关实例组id")
    private Long id;
    @Schema(description = "网关实例组名称")
    private String groupName;
    @Schema(description = "网关实例组唯一key")
    private String groupKey;
}
