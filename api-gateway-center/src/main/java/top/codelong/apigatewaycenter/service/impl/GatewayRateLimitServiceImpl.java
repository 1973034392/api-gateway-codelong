package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayRateLimitDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayRateLimitMapper;
import top.codelong.apigatewaycenter.dto.req.RateLimitConfigReqVO;
import top.codelong.apigatewaycenter.service.GatewayRateLimitService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关限流配置服务实现
 */
@Slf4j
@Service
public class GatewayRateLimitServiceImpl implements GatewayRateLimitService {

    @Resource
    private GatewayRateLimitMapper rateLimitMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UniqueIdUtil uniqueIdUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRateLimitConfig(RateLimitConfigReqVO reqVO) {
        log.info("创建限流配置: {}", reqVO);

        // 构建实体
        GatewayRateLimitDO rateLimitDO = new GatewayRateLimitDO();
        rateLimitDO.setId(uniqueIdUtil.nextId());
        rateLimitDO.setRuleName(reqVO.getRuleName());
        rateLimitDO.setLimitType(reqVO.getLimitType());
        rateLimitDO.setLimitTarget(reqVO.getLimitTarget());
        rateLimitDO.setLimitCount(reqVO.getLimitCount());
        rateLimitDO.setTimeWindow(reqVO.getTimeWindow());
        rateLimitDO.setStatus(reqVO.getStatus() != null ? reqVO.getStatus() : 1);
        rateLimitDO.setStrategy(reqVO.getStrategy() != null ? reqVO.getStrategy() : "TOKEN_BUCKET");
        rateLimitDO.setCreateTime(LocalDateTime.now());
        rateLimitDO.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        rateLimitMapper.insert(rateLimitDO);

        // 同步到Redis
        syncConfigToRedis(rateLimitDO);

        // 通知所有网关节点更新配置
        publishConfigUpdate(rateLimitDO);

        log.info("限流配置创建成功，ID: {}", rateLimitDO.getId());
        return rateLimitDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateRateLimitConfig(RateLimitConfigReqVO reqVO) {
        log.info("更新限流配置: {}", reqVO);

        if (reqVO.getId() == null) {
            throw new IllegalArgumentException("配置ID不能为空");
        }

        GatewayRateLimitDO rateLimitDO = rateLimitMapper.selectById(reqVO.getId());
        if (rateLimitDO == null) {
            throw new IllegalArgumentException("限流配置不存在");
        }

        // 更新字段
        rateLimitDO.setRuleName(reqVO.getRuleName());
        rateLimitDO.setLimitType(reqVO.getLimitType());
        rateLimitDO.setLimitTarget(reqVO.getLimitTarget());
        rateLimitDO.setLimitCount(reqVO.getLimitCount());
        rateLimitDO.setTimeWindow(reqVO.getTimeWindow());
        rateLimitDO.setStatus(reqVO.getStatus());
        rateLimitDO.setStrategy(reqVO.getStrategy());
        rateLimitDO.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        rateLimitMapper.updateById(rateLimitDO);

        // 同步到Redis
        syncConfigToRedis(rateLimitDO);

        // 通知所有网关节点更新配置
        publishConfigUpdate(rateLimitDO);

        log.info("限流配置更新成功，ID: {}", rateLimitDO.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteRateLimitConfig(Long id) {
        log.info("删除限流配置，ID: {}", id);

        GatewayRateLimitDO rateLimitDO = rateLimitMapper.selectById(id);
        if (rateLimitDO == null) {
            throw new IllegalArgumentException("限流配置不存在");
        }

        // 从数据库删除
        rateLimitMapper.deleteById(id);

        // 从Redis删除
        String redisKey = buildRedisKey(rateLimitDO);
        redisTemplate.delete(redisKey);

        // 通知所有网关节点删除配置
        publishConfigUpdate(rateLimitDO);

        log.info("限流配置删除成功，ID: {}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, Integer status) {
        log.info("更新限流配置状态，ID: {}, status: {}", id, status);

        GatewayRateLimitDO rateLimitDO = rateLimitMapper.selectById(id);
        if (rateLimitDO == null) {
            throw new IllegalArgumentException("限流配置不存在");
        }

        rateLimitDO.setStatus(status);
        rateLimitDO.setUpdateTime(LocalDateTime.now());

        // 更新数据库
        rateLimitMapper.updateById(rateLimitDO);

        // 同步到Redis
        syncConfigToRedis(rateLimitDO);

        // 通知所有网关节点更新配置
        publishConfigUpdate(rateLimitDO);

        log.info("限流配置状态更新成功，ID: {}", id);
        return true;
    }

    @Override
    public GatewayRateLimitDO getRateLimitConfig(Long id) {
        return rateLimitMapper.selectById(id);
    }

    @Override
    public PageResult<GatewayRateLimitDO> listRateLimitConfigs(Integer pageNum, Integer pageSize, String limitType) {
        log.info("分页查询限流配置，pageNum: {}, pageSize: {}, limitType: {}", pageNum, pageSize, limitType);

        Page<GatewayRateLimitDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GatewayRateLimitDO> queryWrapper = new LambdaQueryWrapper<>();

        if (limitType != null && !limitType.isEmpty()) {
            queryWrapper.eq(GatewayRateLimitDO::getLimitType, limitType);
        }

        queryWrapper.orderByDesc(GatewayRateLimitDO::getUpdateTime);

        Page<GatewayRateLimitDO> resultPage = rateLimitMapper.selectPage(page, queryWrapper);

        return PageResult.<GatewayRateLimitDO>builder()
                .list(resultPage.getRecords())
                .total(resultPage.getTotal())
                .pageNum((int) resultPage.getCurrent())
                .pageSize((int) resultPage.getSize())
                .build();
    }

    @Override
    public void refreshAllGatewayConfigs() {
        log.info("刷新所有网关节点的限流配置");

        // 查询所有启用的限流配置
        LambdaQueryWrapper<GatewayRateLimitDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GatewayRateLimitDO::getStatus, 1);
        List<GatewayRateLimitDO> configs = rateLimitMapper.selectList(queryWrapper);

        // 同步到Redis
        for (GatewayRateLimitDO config : configs) {
            syncConfigToRedis(config);
        }

        // 通知所有网关节点重新加载配置
        redisTemplate.convertAndSend("rate-limit-config-update", "RELOAD_ALL");

        log.info("限流配置刷新完成，共刷新 {} 条配置", configs.size());
    }

    /**
     * 同步配置到Redis
     */
    private void syncConfigToRedis(GatewayRateLimitDO config) {
        String redisKey = buildRedisKey(config);

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("id", config.getId());
        configMap.put("ruleName", config.getRuleName());
        configMap.put("limitType", config.getLimitType());
        configMap.put("limitTarget", config.getLimitTarget());
        configMap.put("limitCount", config.getLimitCount());
        configMap.put("timeWindow", config.getTimeWindow());
        configMap.put("enabled", config.getStatus() == 1);
        configMap.put("strategy", config.getStrategy());

        redisTemplate.opsForHash().putAll(redisKey, configMap);

        log.debug("限流配置已同步到Redis: {}", redisKey);
    }

    /**
     * 发布配置更新消息
     */
    private void publishConfigUpdate(GatewayRateLimitDO config) {
        Map<String, Object> message = new HashMap<>();
        message.put("id", config.getId());
        message.put("ruleName", config.getRuleName());
        message.put("limitType", config.getLimitType());
        message.put("limitTarget", config.getLimitTarget());
        message.put("limitCount", config.getLimitCount());
        message.put("timeWindow", config.getTimeWindow());
        message.put("enabled", config.getStatus() == 1);
        message.put("strategy", config.getStrategy());

        redisTemplate.convertAndSend("rate-limit-config-update",
            com.alibaba.fastjson.JSON.toJSONString(message));

        log.debug("限流配置更新消息已发布");
    }

    /**
     * 构建Redis键
     */
    private String buildRedisKey(GatewayRateLimitDO config) {
        return "rate_limit_config:" + config.getLimitType() + ":" + config.getLimitTarget();
    }
}

