package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway-server-group-rel")
@RequiredArgsConstructor
@Tag(name = "网关服务实例组关联")
public class GatewayServerGroupRelController {
}
