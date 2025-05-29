package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayServerGroupRelService;

@RestController
@RequestMapping("/gateway-server-group-rel")
@RequiredArgsConstructor
@Tag(name = "网关服务实例组关联")
public class GatewayServerGroupRelController {
    private final GatewayServerGroupRelService gatewayServerGroupRelService;

    @PostMapping("/create")
    @Operation(summary = "网关服务关联创建")
    public Result<Long> create(@RequestBody ServerGroupRelSaveReqVO reqVO){
        return Result.success(gatewayServerGroupRelService.create(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "网关服务关联删除")
    public Result<Boolean> delete(Long id){
        return Result.success(gatewayServerGroupRelService.removeById(id));
    }

}
