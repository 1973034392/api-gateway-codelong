package top.codelong.apigatewaycenter.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义分页请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParam {
    private Integer pageNo;
    private Integer pageSize;
}
