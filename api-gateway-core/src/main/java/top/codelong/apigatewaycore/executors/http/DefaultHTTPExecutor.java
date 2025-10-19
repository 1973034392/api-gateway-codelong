package top.codelong.apigatewaycore.executors.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.net.URIBuilder;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class DefaultHTTPExecutor implements HTTPExecutor {
    private CloseableHttpAsyncClient asyncHttpClient;
    private final ObjectMapper objectMapper;

    public DefaultHTTPExecutor() {
        this.asyncHttpClient = HttpAsyncClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.asyncHttpClient.start();
    }

    public DefaultHTTPExecutor(CloseableHttpAsyncClient asyncClient, ObjectMapper objectMapper) {
        this.asyncHttpClient = asyncClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void setClient(CloseableHttpAsyncClient client) {
        this.asyncHttpClient = client;
    }

    @Override
    public CompletableFuture<Result<?>> execute(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        CompletableFuture<Result<?>> future = new CompletableFuture<>();
        try {
            HTTPTypeEnum httpType = httpStatement.getHttpType();
            log.debug("准备执行{}请求，URL: {}, 参数: {}", httpType, url, parameter);

            SimpleRequestBuilder requestBuilder;
            switch (httpType) {
                case GET:
                    URIBuilder uriBuilder = new URIBuilder(url);
                    if (parameter != null) {
                        parameter.forEach((k, v) -> uriBuilder.addParameter(k, String.valueOf(v)));
                    }
                    URI uri = uriBuilder.build();
                    requestBuilder = SimpleRequestBuilder.get(uri);
                    break;
                case POST:
                case PUT:
                    requestBuilder = (httpType == HTTPTypeEnum.POST) ? SimpleRequestBuilder.post(url) : SimpleRequestBuilder.put(url);
                    if (parameter != null && !parameter.isEmpty()) {
                        String jsonBody = objectMapper.writeValueAsString(parameter);
                        requestBuilder.setBody(jsonBody, ContentType.APPLICATION_JSON);
                    }
                    break;
                case DELETE:
                    requestBuilder = SimpleRequestBuilder.delete(url);
                    break;
                default:
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
                        if (statusCode >= 200 && statusCode < 300) {
                            future.complete(Result.success(responseBody));
                        } else {
                            log.warn("HTTP请求失败，状态码: {}, 响应: {}", statusCode, responseBody);
                            future.complete(Result.error("HTTP请求失败，状态码: " + statusCode));
                        }
                    } catch (Exception e) {
                        failed(e);
                    }
                }

                @Override
                public void failed(Exception ex) {
                    log.error("异步HTTP请求执行失败", ex);
                    future.complete(Result.error("请求失败: " + ex.getMessage()));
                }

                @Override
                public void cancelled() {
                    log.warn("异步HTTP请求被取消");
                    future.cancel(true);
                }
            });
        } catch (URISyntaxException e) {
            log.error("URL语法错误: {}", url, e);
            future.completeExceptionally(new IllegalArgumentException("URL语法错误: " + url, e));
        } catch (Exception e) {
            log.error("构建异步HTTP请求时出错", e);
            future.completeExceptionally(e);
        }
        return future;
    }
}