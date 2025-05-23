package top.codelong.apigatewaycenter.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParam {
    private Integer pageNo;
    private Integer pageSize;
}
