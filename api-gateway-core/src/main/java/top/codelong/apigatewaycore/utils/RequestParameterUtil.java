package top.codelong.apigatewaycore.utils;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestParameterUtil {
    /**
     *  获得请求路径
     */
    public static String getUri(FullHttpRequest request) {
        String uri = request.uri();
        int idx = uri.indexOf("?");
        uri = idx > 0 ? uri.substring(0, idx) : uri;
        if (uri.equals("/favicon.ico")) {
            return null;
        }
        return uri;
    }

    /**
     * 解析封装请求参数
     */
    public static Map<String, Object> getParameters(FullHttpRequest request) {
        // 获取请求类型
        HttpMethod method = request.method();
        if (HttpMethod.GET == method) {
            Map<String, Object> parameterMap = new HashMap<>();
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            decoder.parameters().forEach((key, value) -> parameterMap.put(key, value.get(0)));
            return parameterMap;
        } else if (HttpMethod.POST == method) {
            String contentType = getContentType(request);
            switch (contentType) {
                case "multipart/form-data":
                    Map<String, Object> parameterMap = new HashMap<>();
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                    decoder.offer(request);
                    decoder.getBodyHttpDatas().forEach(data -> {
                        Attribute attr = (Attribute) data;
                        try {
                            parameterMap.put(data.getName(), attr.getValue());
                        } catch (IOException ignore) {
                        }
                    });
                    return parameterMap;
                case "application/json":
                    ByteBuf byteBuf = request.content().copy();
                    if (byteBuf.isReadable()) {
                        String content = byteBuf.toString(StandardCharsets.UTF_8);
                        return JSON.parseObject(content);
                    }
                    break;
                case "none":
                    return new HashMap<>();
                default:
                    throw new RuntimeException("未实现的协议类型 Content-Type：" + contentType);
            }
        } else if (HttpMethod.PUT == method || HttpMethod.DELETE == method) {
            String contentType = getContentType(request);
            switch (contentType) {
                case "multipart/form-data" -> {
                    Map<String, Object> parameterMap = new HashMap<>();
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                    decoder.offer(request);
                    decoder.getBodyHttpDatas().forEach(data -> {
                        Attribute attr = (Attribute) data;
                        try {
                            parameterMap.put(data.getName(), attr.getValue());
                        } catch (IOException ignore) {
                        }
                    });
                    return parameterMap;
                }
                case "application/json" -> {
                    ByteBuf byteBuf = request.content().copy();
                    if (byteBuf.isReadable()) {
                        String content = byteBuf.toString(StandardCharsets.UTF_8);
                        return JSON.parseObject(content);
                    }
                }
                case "none" -> {
                    return new HashMap<>();
                }
                default -> {
                    if (request.content().isReadable()) {
                        throw new RuntimeException("未实现的协议类型 Content-Type：" + contentType);
                    } else {
                        Map<String, Object> parameterMap = new HashMap<>();
                        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
                        decoder.parameters().forEach((key, values) ->
                                parameterMap.put(key, values.get(0)));
                        return parameterMap;
                    }
                }
            }
        }

        throw new RuntimeException("未实现的请求类型 HttpMethod：" + method);
    }

    public static String getToken(FullHttpRequest request) {
        Optional<Map.Entry<String, String>> header = request.headers().entries().stream().filter(
                val -> val.getKey().equals("Authorization")
        ).findAny();
        Map.Entry<String, String> entry = header.orElse(null);
        if (entry == null)
            return null;
        return entry.getValue();
    }

    private static String getContentType(FullHttpRequest request) {
        Optional<Map.Entry<String, String>> header = request.headers().entries().stream().filter(
                val -> val.getKey().equals("Content-Type")
        ).findAny();
        Map.Entry<String, String> entry = header.orElse(null);
        if (entry == null)
            return "none";
        String contentType = entry.getValue();
        int idx = contentType.indexOf(";");
        if (idx > 0) {
            return contentType.substring(0, idx);
        } else {
            return contentType;
        }
    }
}
