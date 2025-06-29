package top.codelong.apigatewaycore.executors;

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

@Slf4j
public class HTTPExecutor implements BaseExecutor {
    private final String url;
    private final HttpStatement httpStatement;
    private final CloseableHttpClient CloseableHttpClient;

    public HTTPExecutor(String url, HttpStatement httpStatement, CloseableHttpClient CloseableHttpClient) {
        this.url = url;
        this.httpStatement = httpStatement;
        this.CloseableHttpClient = CloseableHttpClient;
    }

    @Override
    public Result execute(Map<String, Object> parameter) {
        HTTPTypeEnum httpType = httpStatement.getHttpType();
        HttpUriRequest httpRequest = null;
        String requestUrl = url;
        try {
            // 创建请求对象
            switch (httpType) {
                case GET:
                    if (parameter != null && !parameter.isEmpty()) {
                        requestUrl = buildGetRequestUrl(url, parameter);
                    }
                    httpRequest = new HttpGet(requestUrl);
                    break;
                case POST:
                    HttpPost postRequest = new HttpPost(requestUrl);
                    if (parameter != null) {
                        String jsonBody = JSON.toJSONString(parameter);
                        postRequest.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                    }
                    httpRequest = postRequest;
                    break;
                case PUT:
                    HttpPut putRequest = new HttpPut(requestUrl);
                    if (parameter != null) {
                        String jsonBody = JSON.toJSONString(parameter);
                        putRequest.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                    }
                    httpRequest = putRequest;
                    break;
                case DELETE:
                    httpRequest = new HttpDelete(requestUrl);
                    break;
                default:
                    log.error("请求失败: {}", url);
            }

            // 执行请求并获取响应
            try (CloseableHttpResponse response = CloseableHttpClient.execute(httpRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

                // 返回响应结果
                return new Result<>(statusCode, "", responseBody);
            }
        } catch (Exception e) {
            return Result.error("请求失败: " + e.getMessage());
        }
    }

    // 构建带有参数的 GET 请求 URL
    private String buildGetRequestUrl(String baseUrl, Map<String, Object> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8)).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1); // 移除末尾的 &
        }
        return urlBuilder.toString();
    }
}
