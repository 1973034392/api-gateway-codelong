package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDetailDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerDetailMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.dto.req.ServerDetailRegisterReqVO;
import top.codelong.apigatewaycenter.enums.StatusEnum;
import top.codelong.apigatewaycenter.service.GatewayServerDetailService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 网关服务详情服务实现类
 * @author CodeLong
 * @description 针对表【gateway_server_detail(系统详细信息表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayServerDetailServiceImpl extends ServiceImpl<GatewayServerDetailMapper, GatewayServerDetailDO> implements GatewayServerDetailService {
    private final GatewayServerDetailMapper gatewayServerDetailMapper;
    private final UniqueIdUtil uniqueIdUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GatewayServerMapper gatewayServerMapper;

    /**
     * 注册服务详情信息
     * @param reqVO 服务详情注册请求VO
     * @return 注册是否成功
     * @throws RuntimeException 当注册创建失败时抛出
     */
    @Override
    public Boolean register(ServerDetailRegisterReqVO reqVO) {
        log.info("开始注册服务详情，serverAddress: {}, serverId: {}", reqVO.getServerAddress(), reqVO.getServerId());

        Integer count = gatewayServerDetailMapper.registerIfAbsent(reqVO.getServerAddress());
        if (count > 0) {
            log.info("服务详情已存在，无需重复注册，serverAddress: {}", reqVO.getServerAddress());
            return true;
        }

        GatewayServerDetailDO detailDO = new GatewayServerDetailDO();
        detailDO.setId(uniqueIdUtil.nextId());
        detailDO.setServerId(reqVO.getServerId());
        detailDO.setServerAddress(reqVO.getServerAddress());
        detailDO.setStatus(StatusEnum.ENABLE.getValue());

        try {
            gatewayServerDetailMapper.insert(detailDO);
            log.info("成功注册服务详情，detailId: {}", detailDO.getId());
        } catch (Exception e) {
            log.error("注册服务详情失败，serverAddress: {}", reqVO.getServerAddress(), e);
            throw new RuntimeException("注册创建失败");
        }
        return true;
    }

    /**
     * 下线服务详情
     * @param id 服务详情ID
     * @return 下线是否成功
     * @throws RuntimeException 当服务不存在或下线失败时抛出
     */
    @Override
    public Boolean offline(String id) {
        log.info("开始下线服务详情，detailId: {}", id);

        GatewayServerDetailDO detailDO = gatewayServerDetailMapper.selectById(id);
        if (detailDO == null) {
            log.error("下线服务详情失败，服务不存在，detailId: {}", id);
            throw new RuntimeException("服务不存在");
        }

        detailDO.setStatus(StatusEnum.DISABLE.getValue());
        try {
            gatewayServerDetailMapper.updateById(detailDO);
            log.info("成功下线服务详情，detailId: {}", id);
        } catch (Exception e) {
            log.error("下线服务详情失败，detailId: {}", id, e);
            throw new RuntimeException("下线失败");
        }
        return true;
    }

    /**
     * 保持服务心跳
     * @param reqVO 心跳请求VO
     * @return 心跳处理是否成功
     */
    @Override
    public Boolean keepAlive(HeartBeatReqVO reqVO) {
        log.debug("处理心跳请求，safeKey: {}, addr: {}", reqVO.getSafeKey(), reqVO.getAddr());

        String safeKey = reqVO.getSafeKey();
        String server = gatewayServerMapper.getServerNameBySafeKey(safeKey);
        String redisKey = "heartbeat:server:" + server + ":" + reqVO.getAddr();

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);
        if (entries.isEmpty()) {
            log.debug("首次心跳，创建Redis记录，key: {}", redisKey);

            HashMap<String, Object> map = new HashMap<>();
            map.put("lastTime", LocalDateTime.now().toString());
            map.put("startTime", LocalDateTime.now().toString());
            map.put("url", reqVO.getAddr());
            map.put("weight", 1);

            redisTemplate.opsForHash().putAll(redisKey, map);
            redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);
        } else {
            log.debug("更新心跳时间，key: {}", redisKey);

            redisTemplate.opsForHash().put(redisKey, "lastTime", LocalDateTime.now().toString());
            redisTemplate.expire(redisKey, 30, TimeUnit.SECONDS);
        }

        return true;
    }

    /**
     * 根据服务ID查询实例列表
     * @param serverId 服务ID
     * @return 实例列表
     */
    @Override
    public List<GatewayServerDetailDO> listByServerId(String serverId) {
        log.info("查询服务实例列表，serverId: {}", serverId);

        LambdaQueryWrapper<GatewayServerDetailDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GatewayServerDetailDO::getServerId, serverId);
        queryWrapper.orderByDesc(GatewayServerDetailDO::getCreateTime);

        List<GatewayServerDetailDO> list = this.list(queryWrapper);
        log.info("成功查询服务实例列表，serverId: {}, 实例数: {}", serverId, list.size());

        return list;
    }
}