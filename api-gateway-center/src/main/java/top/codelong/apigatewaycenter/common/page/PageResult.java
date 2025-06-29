package top.codelong.apigatewaycenter.common.page;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 自定义分页请求结果
 * @param <T>
 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;

    private Long total;
}
