package top.codelong.apigatewaycenter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import top.codelong.apigatewaycenter.dto.req.GroupPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayGroupService;

@RestController
@RequestMapping("/gateway-group")
@RequiredArgsConstructor
@Tag(name = "网关实例组")
public class GatewayGroupController {
    private final GatewayGroupService gatewayGroupService;

    @PostMapping("/create")
    @Operation(summary = "创建网关实例组")
    public Result<Long> create(@RequestBody GroupSaveReqVO reqVO) {
        return Result.success(gatewayGroupService.create(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新网关实例组")
    public Result<Boolean> update(@RequestBody GroupSaveReqVO reqVO) {
        return Result.success(gatewayGroupService.update(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除网关实例组")
    public Result<Boolean> delete(@RequestParam Long id) {
        return Result.success(gatewayGroupService.delete(id));
    }

    @GetMapping("/get")
    @Operation(summary = "查询网关实例组")
    public Result<GatewayGroupDO> get(@RequestParam Long id) {
        return Result.success(gatewayGroupService.getById(id));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询网关实例组")
    public Result<PageResult<GroupSaveReqVO>> page(GroupPageReqVO reqVO) {
        return Result.success(gatewayGroupService.page(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "获取所有网关实例组列表")
    public Result<java.util.List<GatewayGroupDO>> list() {
        return Result.success(gatewayGroupService.list());
    }
}
