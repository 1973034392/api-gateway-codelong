package top.codelong.apigatewaycore.utils;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 请求参数工具类
 * 提供HTTP请求参数解析相关功能
 */
@Slf4j
public class RequestParameterUtil {
    /**
     * 获取请求路径（去除查询参数）
     * @param request HTTP请求对象
     * @return 处理后的请求路径，如果是/favicon.ico则返回null
     */
    public static String getUrl(FullHttpRequest request) {
        String uri = request.uri();
        int idx = uri.indexOf("?");
        uri = idx > 0 ? uri.substring(0, idx) : uri;
        if (uri.equals("/favicon.ico")) {
            log.debug("忽略favicon.ico请求");
            return null;
        }
        log.debug("解析请求路径: {}", uri);
        return uri;
    }

    /**
     * 解析HTTP请求参数
     * @param request HTTP请求对象
     * @return 参数键值对Map
     * @throws RuntimeException 当遇到不支持的Content-Type或HttpMethod时抛出
     */
    public static Map<String, Object> getParameters(FullHttpRequest request) {
        HttpMethod method = request.method();
        log.debug("开始解析{}请求参数", method);

        if (HttpMethod.GET == method) {
            // GET请求处理
            Map<String, Object> parameterMap = new HashMap<>();
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            decoder.parameters().forEach((key, value) -> {
                parameterMap.put(key, value.get(0));
                log.trace("GET参数: {}={}", key, value.get(0));
            });
            return parameterMap;

        } else if (HttpMethod.POST == method) {
            // POST请求处理
            String contentType = getContentType(request);
            log.debug("POST请求Content-Type: {}", contentType);

            switch (contentType) {
                case "multipart/form-data":
                    Map<String, Object> parameterMap = new HashMap<>();
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                    decoder.offer(request);
                    decoder.getBodyHttpDatas().forEach(data -> {
                        Attribute attr = (Attribute) data;
                        try {
                            parameterMap.put(data.getName(), attr.getValue());
                            log.trace("表单参数: {}={}", data.getName(), attr.getValue());
                        } catch (IOException ignore) {
                            log.warn("表单参数解析异常", ignore);
                        }
                    });
                    return parameterMap;
                case "application/json":
                    ByteBuf byteBuf = request.content();
                    try {
                        if (byteBuf.isReadable()) {
                            String content = byteBuf.toString(StandardCharsets.UTF_8);
                            log.trace("JSON参数: {}", content);
                            return JSON.parseObject(content);
                        }
                    } finally {
                        // 确保ByteBuf被释放
                        byteBuf.release();
                    }
                    break;
                case "none":
                    log.debug("无Content-Type，返回空参数Map");
                    return new HashMap<>();
                default:
                    log.error("不支持的Content-Type: {}", contentType);
                    throw new RuntimeException("未实现的协议类型 Content-Type：" + contentType);
            }
        } else if (HttpMethod.PUT == method || HttpMethod.DELETE == method) {
            // PUT/DELETE请求处理
            String contentType = getContentType(request);
            log.debug("{}请求Content-Type: {}", method, contentType);

            switch (contentType) {
                case "multipart/form-data" -> {
                    Map<String, Object> parameterMap = new HashMap<>();
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                    decoder.offer(request);
                    decoder.getBodyHttpDatas().forEach(data -> {
                        Attribute attr = (Attribute) data;
                        try {
                            parameterMap.put(data.getName(), attr.getValue());
                            log.trace("表单参数: {}={}", data.getName(), attr.getValue());
                        } catch (IOException ignore) {
                            log.warn("表单参数解析异常", ignore);
                        }
                    });
                    return parameterMap;
                }
                case "application/json" -> {
                    ByteBuf byteBuf = request.content();
                    try {
                        if (byteBuf.isReadable()) {
                            String content = byteBuf.toString(StandardCharsets.UTF_8);
                            log.trace("JSON参数: {}", content);
                            return JSON.parseObject(content);
                        }
                    } finally {
                        // 确保ByteBuf被释放
                        byteBuf.release();
                    }
                }
                case "none" -> {
                    log.debug("无Content-Type，返回空参数Map");
                    return new HashMap<>();
                }
                default -> {
                    if (request.content().isReadable()) {
                        log.error("不支持的Content-Type: {}", contentType);
                        throw new RuntimeException("未实现的协议类型 Content-Type：" + contentType);
                    } else {
                        Map<String, Object> parameterMap = new HashMap<>();
                        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
                        decoder.parameters().forEach((key, values) -> {
                            parameterMap.put(key, values.get(0));
                            log.trace("URL参数: {}={}", key, values.get(0));
                        });
                        return parameterMap;
                    }
                }
            }
        }

        log.error("不支持的HttpMethod: {}", method);
        throw new RuntimeException("未实现的请求类型 HttpMethod：" + method);
    }

    /**
     * 从请求头中获取Token
     * @param request HTTP请求对象
     * @return Token字符串，未找到返回null
     */
    public static String getToken(FullHttpRequest request) {
        Optional<Map.Entry<String, String>> header = request.headers().entries().stream()
                .filter(val -> val.getKey().equals("Authorization"))
                .findAny();

        if (header.isEmpty()) {
            log.debug("请求头中未找到Authorization字段");
            return null;
        }

        String token = header.get().getValue();
        log.debug("从请求头中获取到Token");
        return token;
    }

    /**
     * 获取请求的Content-Type
     * @param request HTTP请求对象
     * @return Content-Type字符串，未找到返回"none"
     */
    private static String getContentType(FullHttpRequest request) {
        Optional<Map.Entry<String, String>> header = request.headers().entries().stream()
                .filter(val -> val.getKey().equals("Content-Type"))
                .findAny();

        if (header.isEmpty()) {
            log.debug("请求头中未找到Content-Type字段");
            return "none";
        }

        String contentType = header.get().getValue();
        int idx = contentType.indexOf(";");
        if (idx > 0) {
            contentType = contentType.substring(0, idx);
        }
        log.trace("解析到Content-Type: {}", contentType);
        return contentType;
    }
}