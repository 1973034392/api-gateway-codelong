package top.codelong.service.impl;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.codelong.findsdk.annotation.ApiInterface;
import top.codelong.findsdk.annotation.ApiMethod;
import top.codelong.findsdk.enums.HttpTypeEnum;
import top.codelong.sendsdk.client.GatewayClient;
import top.codelong.sendsdk.common.Result;
import top.codelong.service.TestService;

import java.util.Map;

@ApiInterface(interfaceName = "测试接口")
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private GatewayClient gatewayClient;

    @ApiMethod(url = "/test")
    public String test(String name) {
        return "1  " + name;
    }

    @ApiMethod(isAuth = 1, isHttp = 1, httpType = HttpTypeEnum.GET, url = "/test2")
    public String test2(String name) {
        String getResult = gatewayClient.get("/test", Map.of("name", "codelong"));
        Result result = JSON.parseObject(getResult, Result.class);
        if (result.isSuccess()) {
            System.out.println("GET请求成功: " + result.getData());
        }
        return name;
    }
}
