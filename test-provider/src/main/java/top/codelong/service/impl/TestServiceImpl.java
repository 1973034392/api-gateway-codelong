package top.codelong.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import top.codelong.findsdk.annotation.ApiInterface;
import top.codelong.findsdk.annotation.ApiMethod;
import top.codelong.findsdk.enums.HttpTypeEnum;
import top.codelong.sendsdk.client.GatewayClient;
import top.codelong.service.TestService;

@ApiInterface(interfaceName = "测试接口")
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private GatewayClient gatewayClient;

    @ApiMethod(url = "/test")
    public String test(String name) {
        return "1  " + name;
    }

    @ApiMethod(isHttp = 1, httpType = HttpTypeEnum.GET, url = "/test2")
    public String test2(String name) {
        return "2  " + name;
    }
}
