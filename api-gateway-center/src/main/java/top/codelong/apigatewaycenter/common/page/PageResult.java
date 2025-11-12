package top.codelong.apigatewaycenter.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 自定义分页请求结果
 * @param <T>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;

    private Long total;

    private Integer pageNum;

    private Integer pageSize;

    /**
     * 兼容旧版本的构造函数（只有list和total）
     */
    public PageResult(List<T> list, Long total) {
        this.list = list;
        this.total = total;
    }
}
