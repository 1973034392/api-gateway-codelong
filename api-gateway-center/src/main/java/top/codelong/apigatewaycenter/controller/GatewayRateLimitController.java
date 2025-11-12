package top.codelong.apigatewaycenter.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.common.result.Result;
import top.codelong.apigatewaycenter.dao.entity.GatewayRateLimitDO;
import top.codelong.apigatewaycenter.dto.req.RateLimitConfigReqVO;
import top.codelong.apigatewaycenter.service.GatewayRateLimitService;

/**
 * 网关限流配置管理接口
 */
@Slf4j
@RestController
@RequestMapping("/gateway-rate-limit")
public class GatewayRateLimitController {

    @Resource
    private GatewayRateLimitService rateLimitService;

    /**
     * 创建限流配置
     */
    @PostMapping("/create")
    public Result<Long> createRateLimitConfig(@RequestBody RateLimitConfigReqVO reqVO) {
        log.info("创建限流配置请求: {}", reqVO);
        try {
            Long id = rateLimitService.createRateLimitConfig(reqVO);
            return Result.success(id);
        } catch (Exception e) {
            log.error("创建限流配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新限流配置
     */
    @PutMapping("/update")
    public Result<Boolean> updateRateLimitConfig(@RequestBody RateLimitConfigReqVO reqVO) {
        log.info("更新限流配置请求: {}", reqVO);
        try {
            Boolean success = rateLimitService.updateRateLimitConfig(reqVO);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新限流配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除限流配置
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteRateLimitConfig(@PathVariable Long id) {
        log.info("删除限流配置请求，ID: {}", id);
        try {
            Boolean success = rateLimitService.deleteRateLimitConfig(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除限流配置失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用/禁用限流配置
     */
    @PutMapping("/status/{id}/{status}")
    public Result<Boolean> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        log.info("更新限流配置状态请求，ID: {}, status: {}", id, status);
        try {
            Boolean success = rateLimitService.updateStatus(id, status);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新限流配置状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询限流配置详情
     */
    @GetMapping("/detail/{id}")
    public Result<GatewayRateLimitDO> getRateLimitConfig(@PathVariable Long id) {
        log.info("查询限流配置详情，ID: {}", id);
        try {
            GatewayRateLimitDO config = rateLimitService.getRateLimitConfig(id);
            return Result.success(config);
        } catch (Exception e) {
            log.error("查询限流配置详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询限流配置列表
     */
    @GetMapping("/list")
    public Result<PageResult<GatewayRateLimitDO>> listRateLimitConfigs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String limitType) {
        log.info("分页查询限流配置列表，pageNum: {}, pageSize: {}, limitType: {}", pageNum, pageSize, limitType);
        try {
            PageResult<GatewayRateLimitDO> result = rateLimitService.listRateLimitConfigs(pageNum, pageSize, limitType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询限流配置列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 刷新所有网关节点的限流配置
     */
    @PostMapping("/refresh")
    public Result<Void> refreshAllGatewayConfigs() {
        log.info("刷新所有网关节点的限流配置");
        try {
            rateLimitService.refreshAllGatewayConfigs();
            return Result.success();
        } catch (Exception e) {
            log.error("刷新限流配置失败", e);
            return Result.error(e.getMessage());
        }
    }
}

