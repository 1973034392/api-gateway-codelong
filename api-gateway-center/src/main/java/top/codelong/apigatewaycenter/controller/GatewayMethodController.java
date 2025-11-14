package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;
import top.codelong.apigatewaycenter.service.GatewayMethodService;

import java.util.List;

@RestController
@RequestMapping("/gateway-method")
@RequiredArgsConstructor
@Tag(name = "网关方法")
public class GatewayMethodController {
    private final GatewayMethodService gatewayMethodService;

    @GetMapping("/list/{interfaceId}")
    @Operation(description = "根据接口ID获取方法列表")
    public Result<List<GatewayMethodDO>> listByInterfaceId(@PathVariable String interfaceId) {
        return Result.success(gatewayMethodService.listByInterfaceId(interfaceId));
    }
}
