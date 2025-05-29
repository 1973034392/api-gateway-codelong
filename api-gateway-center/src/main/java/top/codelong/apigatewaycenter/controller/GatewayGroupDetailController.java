package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayGroupDetailService;

@RestController
@RequestMapping("/gateway-group-detail")
@RequiredArgsConstructor
@Tag(name = "网关实例组详情")
public class GatewayGroupDetailController {
    private final GatewayGroupDetailService gatewayGroupDetailService;

    @PostMapping("/create")
    @Operation(summary = "创建网关实例组详情")
    public Result<Long> create(@RequestBody GroupDetailSaveReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新网关实例组详情")
    public Result<Boolean> update(@RequestBody GroupDetailSaveReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.update(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除网关实例组详情")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(gatewayGroupDetailService.delete(id));
    }

    @GetMapping("/get")
    @Operation(summary = "查询网关实例组详情")
    public Result<GroupDetailSaveReqVO> get(@RequestParam Long id) {
        return Result.success(gatewayGroupDetailService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询网关实例组详情")
    public Result<PageResult<GroupDetailSaveReqVO>> page(GroupDetailPageReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.page(reqVO));
    }

    @PutMapping("/update/status")
    @Operation(summary = "更新网关实例组详情状态")
    public Result<Boolean> updateStatus(@RequestParam Long id) {
        return Result.success(gatewayGroupDetailService.updateStatus(id));
    }
}
