package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dto.req.InterfaceMethodSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayInterfaceService;

@RestController
@RequestMapping("/gateway-interface")
@RequiredArgsConstructor
@Tag(name = "网关接口")
public class GatewayInterfaceController {
    private final GatewayInterfaceService gatewayInterfaceService;

    @PostMapping("/create")
    @Operation(description = "保存网关接口和方法信息")
    private Result<Long> create(@RequestBody InterfaceMethodSaveReqVO reqVO) {
        return Result.success(gatewayInterfaceService.create(reqVO));
    }
}