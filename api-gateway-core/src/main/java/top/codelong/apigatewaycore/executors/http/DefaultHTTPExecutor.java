package top.codelong.apigatewaycore.executors.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import top.codelong.apigatewaycore.common.HttpStatement;
import top.codelong.apigatewaycore.common.result.Result;
import top.codelong.apigatewaycore.enums.HTTPTypeEnum;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP请求执行器
 * 负责执行各种类型的HTTP请求(GET/POST/PUT/DELETE)
 */
@Slf4j
public class DefaultHTTPExecutor implements HTTPExecutor {
    private volatile CloseableHttpClient closeableHttpClient;

    public void setClient(CloseableHttpClient client) {
        if (this.closeableHttpClient == null) {
            synchronized (DefaultHTTPExecutor.class) {
                if (this.closeableHttpClient == null) {
                    try {
                        this.closeableHttpClient = client;
                    } catch (Exception ignore) {
                    }
                }
            }
        }
    }

    /**
     * 执行HTTP请求
     *
     * @param parameter 请求参数
     * @return 执行结果
     */
    @Override
    public Result execute(Map<String, Object> parameter, String url, HttpStatement httpStatement) {
        HTTPTypeEnum httpType = httpStatement.getHttpType();
        HttpUriRequest httpRequest;
        String requestUrl = url;

        try {
            log.debug("准备执行{}请求，URL: {}, 参数: {}", httpType, url, parameter);

            // 根据请求类型创建不同的请求对象
            switch (httpType) {
                case GET:
                    if (parameter != null && !parameter.isEmpty()) {
                        requestUrl = buildGetRequestUrl(url, parameter);
                        log.debug("构建GET请求URL: {}", requestUrl);
                    }
                    httpRequest = new HttpGet(requestUrl);
                    break;
                case POST:
                    HttpPost postRequest = new HttpPost(requestUrl);
                    if (parameter != null) {
                        String jsonBody = JSON.toJSONString(parameter);
                        postRequest.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                        log.trace("POST请求体: {}", jsonBody);
                    }
                    httpRequest = postRequest;
                    break;
                case PUT:
                    HttpPut putRequest = new HttpPut(requestUrl);
                    if (parameter != null) {
                        String jsonBody = JSON.toJSONString(parameter);
                        putRequest.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                        log.trace("PUT请求体: {}", jsonBody);
                    }
                    httpRequest = putRequest;
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(requestUrl);
                    break;
                default:
                    log.error("不支持的HTTP请求类型: {}", httpType);
                    return Result.error("不支持的HTTP请求类型: " + httpType);
            }

            // 执行请求并处理响应
            try (CloseableHttpResponse response = closeableHttpClient.execute(httpRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

                log.debug("HTTP请求完成，状态码: {}, 响应体长度: {}", statusCode, responseBody.length());
                log.trace("完整响应体: {}", responseBody);

                return new Result<>(statusCode, "", responseBody);
            }
        } catch (Exception e) {
            log.error("HTTP请求执行失败，URL: {}, 错误: {}", requestUrl, e.getMessage(), e);
            return Result.error("请求失败: " + e.getMessage());
        }
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