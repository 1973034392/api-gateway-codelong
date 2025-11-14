package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupRegisterReqVO;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.dto.resp.GroupDetailRegisterRespVO;
import top.codelong.apigatewaycenter.service.GatewayGroupDetailService;

@RestController
@RequestMapping("/gateway-group-detail")
@RequiredArgsConstructor
@Tag(name = "网关实例组详情")
public class GatewayGroupDetailController {
    private final GatewayGroupDetailService gatewayGroupDetailService;

    @PostMapping("/create")
    @Operation(summary = "创建网关实例组详情")
    public Result<String> create(@RequestBody GroupDetailSaveReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新网关实例组详情")
    public Result<Boolean> update(@RequestBody GroupDetailSaveReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.update(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除网关实例组详情")
    public Result<Boolean> delete(@RequestParam String id) {
        return Result.success(gatewayGroupDetailService.delete(id));
    }

    @GetMapping("/get")
    @Operation(summary = "查询网关实例组详情")
    public Result<GroupDetailSaveReqVO> get(@RequestParam String id) {
        return Result.success(gatewayGroupDetailService.get(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询网关实例组详情")
    public Result<PageResult<GroupDetailSaveReqVO>> page(GroupDetailPageReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.page(reqVO));
    }

    @PutMapping("/update/status")
    @Operation(summary = "更新网关实例组详情状态")
    public Result<Boolean> updateStatus(@RequestParam String id) {
        return Result.success(gatewayGroupDetailService.updateStatus(id));
    }

    @GetMapping("/get/server-name")
    @Operation(summary = "获得当前实例服务名")
    public Result<String> getServerName(String groupKey) {
        return Result.success(gatewayGroupDetailService.getServerName(groupKey));
    }

    @PostMapping("/register")
    @Operation(summary = "注册网关实例组详情")
    public Result<GroupDetailRegisterRespVO> register(@RequestBody GroupRegisterReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.register(reqVO));
    }

    @PutMapping("/keep-alive")
    @Operation(summary = "维持网关实例组详情心跳")
    public Result<String> keepAlive(@RequestBody HeartBeatReqVO reqVO) {
        return Result.success(gatewayGroupDetailService.keepAlive(reqVO));
    }

    @GetMapping("/list/{groupId}")
    @Operation(summary = "根据分组ID获取实例列表")
    public Result<java.util.List<GroupDetailSaveReqVO>> list(@PathVariable String groupId) {
        return Result.success(gatewayGroupDetailService.listByGroupId(groupId));
    }
}