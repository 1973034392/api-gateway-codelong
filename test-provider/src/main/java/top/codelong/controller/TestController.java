package top.codelong.controller;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.codelong.service.TestService;

@RestController
@RequestMapping
public class TestController {
    @Resource
    private TestService testService;

    @GetMapping("/test")
    public String test(@RequestParam("name") String name) {
        return testService.test(name);
    }

    @GetMapping("/test2")
    public String test2(@RequestParam("name") String name) {
        return testService.test2(name);
    }
}
