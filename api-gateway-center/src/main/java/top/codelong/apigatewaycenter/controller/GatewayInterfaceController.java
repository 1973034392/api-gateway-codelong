package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
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
    public Result<String> create(@RequestBody InterfaceMethodSaveReqVO reqVO) {
        return Result.success(gatewayInterfaceService.create(reqVO));
    }

    @GetMapping("/page")
    @Operation(description = "分页查询网关接口列表")
    public Result<PageResult<?>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String serverId) {
        return Result.success(gatewayInterfaceService.page(pageNum, pageSize, serverId));
    }

    @GetMapping("/list")
    @Operation(description = "获取所有网关接口列表")
    public Result<java.util.List<?>> list() {
        return Result.success(gatewayInterfaceService.list());
    }
}