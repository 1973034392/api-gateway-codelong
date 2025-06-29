package top.codelong.service.impl;

import org.springframework.stereotype.Service;
import top.codelong.findsdk.annotation.ApiInterface;
import top.codelong.findsdk.annotation.ApiMethod;
import top.codelong.service.Test2Service;

@Service
@ApiInterface(interfaceName = "测试接口2")
public class Test2ServiceImpl implements Test2Service {
    @ApiMethod(url = "/test123")
    public String test(String name) {
        return "2  " + name;
    }
}
