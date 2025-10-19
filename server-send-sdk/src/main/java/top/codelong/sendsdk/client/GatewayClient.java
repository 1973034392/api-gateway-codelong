package top.codelong.sendsdk.client;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.codelong.sendsdk.common.enums.HttpMethod;
import top.codelong.sendsdk.common.exception.GatewayException;
import top.codelong.sendsdk.config.GatewaySDKProperties;
import top.codelong.sendsdk.utils.JwtUtils;

import java.io.IOException;
import java.util.Map;

/**
 * 网关客户端核心类
 * 提供向网关发送请求的API
 */
@Component
public class GatewayClient {
    private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

    private final GatewaySDKProperties properties;
    private final ObjectMapper objectMapper;
    private final JwtUtils jwtUtils;
    private final CloseableHttpClient httpClient;

    public GatewayClient(GatewaySDKProperties properties,
                         ObjectMapper objectMapper,
                         JwtUtils jwtUtils,
                         CloseableHttpClient httpClient) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.jwtUtils = jwtUtils;
        this.httpClient = httpClient;
    }

    public String get(String path, Map<String, Object> params) throws GatewayException {
        return execute(HttpMethod.GET, path, params, null);
    }

    public String post(String path, Map<String, Object> params, Object body) throws GatewayException {
        return execute(HttpMethod.POST, path, params, body);
    }

    public String put(String path, Map<String, Object> params, Object body) throws GatewayException {
        return execute(HttpMethod.PUT, path, params, body);
    }

    public String delete(String path, Map<String, Object> params) throws GatewayException {
        return execute(HttpMethod.DELETE, path, params, null);
    }

    private String execute(HttpMethod method, String path,
                           Map<String, Object> params, Object body) throws GatewayException {
        try {
            String url = buildUrl(path, params);
            log.debug("请求网关: {} {}", method, url);

            HttpUriRequest request = createRequest(method, url, body);
            setAuthHeader(request);

            HttpResponse response = httpClient.execute(request);
            return handleResponse(response);
        } catch (IOException e) {
            log.error("网关请求失败: {}", e.getMessage(), e);
            throw new GatewayException("网关请求失败: " + e.getMessage(), e);
        }
    }

    private String buildUrl(String path, Map<String, Object> params) {
        StringBuilder urlBuilder = new StringBuilder(properties.getBaseUrl());

        if (path != null && !path.startsWith("/")) {
            urlBuilder.append("/");
        }
        urlBuilder.append(path);

        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }

        return urlBuilder.toString();
    }

    private HttpUriRequest createRequest(HttpMethod method, String url, Object body) throws IOException {
        switch (method) {
            case GET:
                return new HttpGet(url);
            case POST:
                return createEntityRequest(new HttpPost(url), body);
            case PUT:
                return createEntityRequest(new HttpPut(url), body);
            case DELETE:
                return new HttpDelete(url);
            default:
                throw new IllegalArgumentException("不支持的HTTP方法: " + method);
        }
    }

    private HttpEntityEnclosingRequestBase createEntityRequest(
            HttpEntityEnclosingRequestBase request, Object body) throws IOException {
        if (body != null) {
            String jsonBody = objectMapper.writeValueAsString(body);
            StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            log.trace("请求体: {}", jsonBody);
        }
        return request;
    }

    private void setAuthHeader(HttpUriRequest request) {
        String token = jwtUtils.generateToken(properties.getSafeKey(), properties.getSafeSecret());
        request.setHeader("Authorization", "Bearer " + token);
        log.trace("设置认证头: Bearer {}", token);
    }

    private String handleResponse(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new GatewayException("网关返回错误状态码: " + statusCode);
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new GatewayException("网关返回空响应");
        }

        String responseBody = EntityUtils.toString(entity, "UTF-8");
        log.trace("网关响应: {}", responseBody);

        JSONObject jsonObject = new JSONObject(responseBody);
        if (!jsonObject.containsKey("code") || !jsonObject.containsKey("msg")) {
            throw new GatewayException("网关返回无效响应");
        }
        if (!jsonObject.getInt("code").equals(1)) {
            throw new GatewayException("网关返回错误: " + jsonObject.getStr("msg"));
        }

        return jsonObject.getStr("data");
    }
}