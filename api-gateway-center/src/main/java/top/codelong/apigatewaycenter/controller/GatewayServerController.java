package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dto.req.ServerPageReqVO;
import top.codelong.apigatewaycenter.dto.req.ServerSaveReqVO;
import top.codelong.apigatewaycenter.service.impl.GatewayServerService;

@RestController
@RequestMapping("/gateway-server")
@RequiredArgsConstructor
@Tag(name = "网关服务")
public class GatewayServerController {
    private final GatewayServerService gatewayServerService;

    @PostMapping("/create")
    @Operation(description = "创建网关服务")
    public Result<Long> create(@RequestBody ServerSaveReqVO reqVO) {
        return Result.success(gatewayServerService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(description = "更新网关服务")
    public Result<Boolean> update(@RequestBody ServerSaveReqVO reqVO) {
        return Result.success(gatewayServerService.update(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(description = "删除网关服务")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(gatewayServerService.removeById(id));
    }

    @PutMapping("/update/status")
    @Operation(description = "更新网关服务状态")
    public Result<Boolean> updateStatus(@RequestParam Long id) {
        return Result.success(gatewayServerService.updateStatus(id));
    }

    @GetMapping("/get")
    @Operation(description = "查询网关服务")
    public Result<ServerSaveReqVO> get(@RequestParam Long id) {
        return Result.success(gatewayServerService.get(id));
    }

    @GetMapping("/page")
    @Operation(description = "分页查询网关服务")
    public Result<PageResult<GatewayServerDO>> page(ServerPageReqVO reqVO) {
        return Result.success(gatewayServerService.page(reqVO));
    }
}
