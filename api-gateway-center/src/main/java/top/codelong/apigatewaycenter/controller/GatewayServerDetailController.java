package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.dto.req.ServerDetailRegisterReqVO;
import top.codelong.apigatewaycenter.service.GatewayServerDetailService;

@RestController
@RequestMapping("/gateway-server-detail")
@RequiredArgsConstructor
@Tag(name = "网关服务详情")
public class GatewayServerDetailController {
    private final GatewayServerDetailService gatewayServerDetailService;

    @PostMapping("/register")
    @Operation(summary = "服务详情注册")
    public Result<Boolean> register(@RequestBody ServerDetailRegisterReqVO reqVO) {
        return Result.success(gatewayServerDetailService.register(reqVO));
    }

    @PutMapping("/offline")
    @Operation(summary = "服务下线")
    public Result<Boolean> offline(@RequestParam Long id) {
        return Result.success(gatewayServerDetailService.offline(id));
    }

    @PutMapping("/keep-alive")
    @Operation(summary = "维持网关实例组详情心跳")
    public Result<Boolean> keepAlive(@RequestBody HeartBeatReqVO reqVO) {
        return Result.success(gatewayServerDetailService.keepAlive(reqVO));
    }

    //TODO 服务下线
}