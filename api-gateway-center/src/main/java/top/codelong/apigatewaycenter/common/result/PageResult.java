package top.codelong.apigatewaycenter.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private List<T> list;

    private Integer total;

}
