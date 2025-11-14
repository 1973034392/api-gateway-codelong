package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;
import top.codelong.apigatewaycenter.dto.resp.ServerGroupRelRespVO;
import top.codelong.apigatewaycenter.service.GatewayServerGroupRelService;

@RestController
@RequestMapping("/gateway-server-group-rel")
@RequiredArgsConstructor
@Tag(name = "网关服务实例组关联")
public class GatewayServerGroupRelController {
    private final GatewayServerGroupRelService gatewayServerGroupRelService;

    @PostMapping("/create")
    @Operation(summary = "网关服务关联创建")
    public Result<String> create(@RequestBody ServerGroupRelSaveReqVO reqVO){
        return Result.success(gatewayServerGroupRelService.create(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "网关服务关联删除")
    public Result<Boolean> delete(String id){
        return Result.success(gatewayServerGroupRelService.removeById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询网关服务关联列表")
    public Result<PageResult<ServerGroupRelRespVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(gatewayServerGroupRelService.page(pageNo, pageSize));
    }

}
