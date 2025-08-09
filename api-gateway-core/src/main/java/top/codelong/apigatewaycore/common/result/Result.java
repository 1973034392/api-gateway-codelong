package top.codelong.apigatewaycore.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 核心服务统一返回结果封装类
 *
 * @param <T> 返回数据泛型类型
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态编码：1成功，0和其它数字为失败
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 成功响应（无数据）
     *
     * @param <T> 泛型类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.data = null;
        result.msg = "";
        result.code = 200;
        return result;
    }

    /**
     * 成功响应（带数据）
     *
     * @param object 返回数据对象
     * @param <T>    泛型类型
     * @return 成功响应结果
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.msg = "";
        result.code = 200;
        return result;
    }

    /**
     * 错误响应
     *
     * @param msg 错误信息
     * @param <T> 泛型类型
     * @return 错误响应结果
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.data = null;
        result.code = 0;
        return result;
    }
}