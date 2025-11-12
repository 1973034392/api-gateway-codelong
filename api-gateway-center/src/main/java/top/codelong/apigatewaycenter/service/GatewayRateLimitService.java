package top.codelong.apigatewaycenter.service;

import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayRateLimitDO;
import top.codelong.apigatewaycenter.dto.req.RateLimitConfigReqVO;

/**
 * 网关限流配置服务接口
 */
public interface GatewayRateLimitService {

    /**
     * 创建限流配置
     * @param reqVO 限流配置请求
     * @return 配置ID
     */
    Long createRateLimitConfig(RateLimitConfigReqVO reqVO);

    /**
     * 更新限流配置
     * @param reqVO 限流配置请求
     * @return 是否成功
     */
    Boolean updateRateLimitConfig(RateLimitConfigReqVO reqVO);

    /**
     * 删除限流配置
     * @param id 配置ID
     * @return 是否成功
     */
    Boolean deleteRateLimitConfig(Long id);

    /**
     * 启用/禁用限流配置
     * @param id 配置ID
     * @param status 状态：0-禁用，1-启用
     * @return 是否成功
     */
    Boolean updateStatus(Long id, Integer status);

    /**
     * 查询限流配置详情
     * @param id 配置ID
     * @return 限流配置
     */
    GatewayRateLimitDO getRateLimitConfig(Long id);

    /**
     * 分页查询限流配置列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param limitType 限流类型（可选）
     * @return 分页结果
     */
    PageResult<GatewayRateLimitDO> listRateLimitConfigs(Integer pageNum, Integer pageSize, String limitType);

    /**
     * 刷新所有网关节点的限流配置
     */
    void refreshAllGatewayConfigs();
}

