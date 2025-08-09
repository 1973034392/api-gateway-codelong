package top.codelong.apigatewaycore.executors.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP请求执行器
 * 负责执行各种类型的HTTP请求(GET/POST/PUT/DELETE)
 */
@Slf4j
public class DefaultHTTPExecutor implements HTTPExecutor {
    private volatile CloseableHttpAsyncClient asyncHttpClient;

    public void setClient(CloseableHttpAsyncClient asyncClient) {
        if (this.asyncHttpClient == null) {
            synchronized (DefaultHTTPExecutor.class) {
                if (this.asyncHttpClient == null) {
                    this.asyncHttpClient = asyncClient;
                }
            }
        }
    }

    /**
     * 异步执行HTTP请求
     *
     * @param parameter 请求参数
     * @return 包含执行结果的CompletableFuture
     */
    @Override
    public CompletableFuture<Result> execute(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        HTTPTypeEnum httpType = httpStatement.getHttpType();
        String requestUrl = url;
        CompletableFuture<Result> future = new CompletableFuture<>();

        try {
            log.debug("准备执行{}请求，URL: {}, 参数: {}", httpType, url, parameter);
            SimpleRequestBuilder requestBuilder;
            // 根据请求类型创建不同的请求对象
            switch (httpType) {
                case GET:
                    if (parameter != null && !parameter.isEmpty()) {
                        requestUrl = buildGetRequestUrl(url, parameter);
                        log.debug("构建GET请求URL: {}", requestUrl);
                    }
                    requestBuilder = SimpleRequestBuilder.get(requestUrl);
                    break;
                case POST:
                    requestBuilder = SimpleRequestBuilder.post(requestUrl);
                    if (parameter != null) {
                        String jsonBody = JSON.toJSONString(parameter);
                        requestBuilder = SimpleRequestBuilder.post(requestUrl).setUri(requestUrl)
                                .setBody(jsonBody, org.apache.hc.core5.http.ContentType.APPLICATION_JSON);
                    }
                    break;
                case PUT:
                    requestBuilder = SimpleRequestBuilder.put(requestUrl);
                    if (parameter != null) {
                        String jsonBody = JSON.toJSONString(parameter);
                        requestBuilder = SimpleRequestBuilder.post(requestUrl).setUri(requestUrl)
                                .setBody(jsonBody, org.apache.hc.core5.http.ContentType.APPLICATION_JSON);
                    }
                    break;
                case DELETE:
                    requestBuilder = SimpleRequestBuilder.delete().setUri(requestUrl);
                    break;
                default:
                    log.error("不支持的HTTP请求类型: {}", httpType);
                    future.completeExceptionally(new IllegalArgumentException("不支持的HTTP请求类型: " + httpType));
                    return future;
            }

            SimpleHttpRequest httpRequest = requestBuilder.build();
            asyncHttpClient.execute(httpRequest, new FutureCallback<>() {
                @Override
                public void completed(SimpleHttpResponse response) {
                    try {
                        int statusCode = response.getCode();
                        String responseBody = response.getBodyText();
                        log.debug("异步HTTP请求完成，状态码: {}", statusCode);
                        future.complete(new Result<>(statusCode, "", responseBody));
                    } catch (Exception e) {
                        failed(e);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    log.error("异步HTTP请求执行失败，错误: {}", ex.getMessage(), ex);
                    future.complete(Result.error("请求失败: " + ex.getMessage()));
                }

                @Override
                public void cancelled() {
                    log.warn("异步HTTP请求被取消");
                    future.cancel(true);
                }
            });
        } catch (Exception e) {
            log.error("构建异步HTTP请求时出错", e);
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * 构建带参数的GET请求URL
     *
     * @param baseUrl 基础URL
     * @param params  参数Map
     * @return 构建完成的URL
     */
    private String buildGetRequestUrl(String baseUrl, Map<String, Object> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                        .append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1); // 移除末尾的&
        }
        return urlBuilder.toString();
    }
}