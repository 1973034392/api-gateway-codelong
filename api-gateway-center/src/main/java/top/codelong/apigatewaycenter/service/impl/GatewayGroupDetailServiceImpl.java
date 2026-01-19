package top.codelong.apigatewaycenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDetailDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupDetailMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupRegisterReqVO;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.dto.resp.GroupDetailRegisterRespVO;
import top.codelong.apigatewaycenter.enums.StatusEnum;
import top.codelong.apigatewaycenter.service.GatewayGroupDetailService;
import top.codelong.apigatewaycenter.utils.NginxConfUtil;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description 针对表【gateway_group_detail(网关实例信息表)】的数据库操作Service实现
 * 提供网关实例的增删改查、注册、心跳检测等功能
 * @createDate 2025-05-23 16:05:44
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GatewayGroupDetailServiceImpl extends ServiceImpl<GatewayGroupDetailMapper, GatewayGroupDetailDO> implements GatewayGroupDetailService {

    private final GatewayGroupDetailMapper gatewayGroupDetailMapper;
    private final GatewayGroupMapper gatewayGroupMapper;
    private final GatewayServerMapper gatewayServerMapper;
    private final UniqueIdUtil uniqueIdUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NginxConfUtil nginxConfUtil;
    public final RedisPubUtil redisPubUtil;

    /**
     * 创建新的网关实例
     *
     * @param reqVO 创建请求参数
     * @return 创建的网关实例ID
     */
    @Override
    public String create(GroupDetailSaveReqVO reqVO) {
        log.info("开始创建新网关实例，请求参数：{}", reqVO);

        String key = reqVO.getGroupKey();
        if (StrUtil.isBlank(key)) {
            log.warn("创建网关实例失败：未选择所属网关实例");
            throw new RuntimeException("请选择所属网关实例");
        }

        String groupId = gatewayGroupMapper.getIdByKey(key);
        if (groupId == null) {
            log.warn("创建网关实例失败：网关实例不存在，groupKey={}", key);
            throw new RuntimeException("网关实例不存在");
        }

        String id = gatewayGroupDetailMapper.getIdByAddr(reqVO.getAddress());
        if (id != null) {
            log.info("网关实例已存在，直接返回ID：{}", id);
            return id;
        }

        GatewayGroupDetailDO gatewayGroupDetailDO = new GatewayGroupDetailDO();
        gatewayGroupDetailDO.setId(uniqueIdUtil.nextId());
        gatewayGroupDetailDO.setGroupId(groupId);
        gatewayGroupDetailDO.setDetailName(reqVO.getName());
        gatewayGroupDetailDO.setDetailAddress(reqVO.getAddress());
        gatewayGroupDetailDO.setDetailWeight(reqVO.getWeight());
        gatewayGroupDetailDO.setStatus(StatusEnum.ENABLE.getValue());

        gatewayGroupDetailMapper.insert(gatewayGroupDetailDO);
        log.info("网关实例创建成功，ID：{}", gatewayGroupDetailDO.getId());

        return gatewayGroupDetailDO.getId();
    }

    /**
     * 更新网关实例信息
     *
     * @param reqVO 更新请求参数
     * @return 操作结果
     */
    @Override
    public Boolean update(GroupDetailSaveReqVO reqVO) {
        log.info("开始更新网关实例，ID：{}", reqVO.getId());

        String key = reqVO.getGroupKey();
        if (StrUtil.isBlank(key)) {
            log.warn("更新网关实例失败：未选择所属网关组");
            throw new RuntimeException("请选择所属网关组");
        }

        String groupId = gatewayGroupMapper.getIdByKey(key);
        if (groupId == null) {
            log.warn("更新网关实例失败：网关组实例不存在，groupKey={}", key);
            throw new RuntimeException("网关组实例不存在");
        }

        GatewayGroupDetailDO gatewayGroupDetailDO = new GatewayGroupDetailDO();
        gatewayGroupDetailDO.setId(reqVO.getId());
        gatewayGroupDetailDO.setDetailName(reqVO.getName());
        gatewayGroupDetailDO.setDetailAddress(reqVO.getAddress());
        gatewayGroupDetailDO.setDetailWeight(reqVO.getWeight());
        gatewayGroupDetailDO.setStatus(null);
        gatewayGroupDetailDO.setGroupId(groupId);

        gatewayGroupDetailMapper.updateById(gatewayGroupDetailDO);
        log.info("网关实例更新成功，ID：{}", reqVO.getId());

        return true;
    }

    /**
     * 删除网关实例
     *
     * @param id 要删除的网关实例ID
     * @return 操作结果
     */
    @Override
    public Boolean delete(String id) {
        log.info("开始删除网关实例，ID：{}", id);

        if (id == null) {
            log.warn("删除网关实例失败：ID为空");
            throw new RuntimeException("网关实例组详情id不能为空");
        }

        GatewayGroupDetailDO gatewayGroupDetailDO = gatewayGroupDetailMapper.selectById(id);
        if (gatewayGroupDetailDO == null) {
            log.warn("要删除的网关实例不存在，ID：{}", id);
            throw new RuntimeException("网关实例组详情不存在");
        }

        gatewayGroupDetailMapper.deleteById(id);
        log.info("网关实例删除成功，ID：{}", id);

        return true;
    }

    /**
     * 获取网关实例详情
     *
     * @param id 要查询的网关实例ID
     * @return 网关实例信息
     */
    @Override
    public GroupDetailSaveReqVO get(String id) {
        log.info("开始获取网关实例详情，ID：{}", id);

        if (id == null) {
            log.warn("获取网关实例详情失败：ID为空");
            throw new RuntimeException("网关实例组详情id不能为空");
        }

        GatewayGroupDetailDO gatewayGroupDetailDO = gatewayGroupDetailMapper.selectById(id);
        if (gatewayGroupDetailDO == null) {
            log.warn("要查询的网关实例不存在，ID：{}", id);
            throw new RuntimeException("网关实例组详情不存在");
        }

        String key = gatewayGroupMapper.selectById(gatewayGroupDetailDO.getGroupId()).getGroupKey();
        GroupDetailSaveReqVO groupDetailSaveReqVO = new GroupDetailSaveReqVO();
        groupDetailSaveReqVO.setId(gatewayGroupDetailDO.getId());
        groupDetailSaveReqVO.setName(gatewayGroupDetailDO.getDetailName());
        groupDetailSaveReqVO.setAddress(gatewayGroupDetailDO.getDetailAddress());
        groupDetailSaveReqVO.setWeight(gatewayGroupDetailDO.getDetailWeight());
        groupDetailSaveReqVO.setStatus(gatewayGroupDetailDO.getStatus());
        groupDetailSaveReqVO.setGroupKey(key);

        log.info("成功获取网关实例详情，ID：{}", id);
        return groupDetailSaveReqVO;
    }

    /**
     * 更新网关实例状态（启用/禁用）
     *
     * @param id 要更新的网关实例ID
     * @return 操作结果
     */
    @Override
    public Boolean updateStatus(String id) {
        log.info("开始更新网关实例状态，ID：{}", id);

        if (id == null) {
            log.warn("更新网关实例状态失败：ID为空");
            throw new RuntimeException("网关实例组详情id不能为空");
        }

        GatewayGroupDetailDO gatewayGroupDetailDO = gatewayGroupDetailMapper.selectById(id);
        if (gatewayGroupDetailDO == null) {
            log.warn("更新网关实例状态失败：实例不存在，ID：{}", id);
            throw new RuntimeException("网关实例组详情不存在");
        }

        Integer status = gatewayGroupDetailDO.getStatus();
        if (status.equals(StatusEnum.DISABLE.getValue())) {
            gatewayGroupDetailDO.setStatus(StatusEnum.ENABLE.getValue());
        } else {
            gatewayGroupDetailDO.setStatus(StatusEnum.DISABLE.getValue());
        }

        gatewayGroupDetailMapper.updateById(gatewayGroupDetailDO);
        log.info("网关实例状态更新成功，ID：{}，新状态：{}", id, gatewayGroupDetailDO.getStatus());

        return true;
    }

    /**
     * 分页查询网关实例
     *
     * @param reqVO 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult<GroupDetailSaveReqVO> page(GroupDetailPageReqVO reqVO) {
        log.info("开始分页查询网关实例，分页参数：{}", reqVO);

        Page<GroupDetailSaveReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        List<GroupDetailSaveReqVO> list = gatewayGroupDetailMapper.pageInfo(page, reqVO);

        log.info("成功完成网关实例分页查询，共查询到{}条记录", list.size());
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 根据网关组Key获取服务器名称
     *
     * @param groupKey 网关组Key
     * @return 服务器名称
     */
    @Override
    public String getServerName(String groupKey) {
        log.info("开始根据网关组Key获取服务器名称，groupKey={}", groupKey);

        if (StrUtil.isBlank(groupKey)) {
            log.warn("获取服务器名称失败：groupKey为空");
            throw new RuntimeException("请选择所属网关组");
        }

        String serverName = gatewayGroupMapper.getServerNameByGroupKey(groupKey);
        log.info("成功获取服务器名称，groupKey={}，serverName={}", groupKey, serverName);

        return serverName;
    }

    /**
     * 注册网关实例
     *
     * @param reqVO 注册请求参数
     * @return 注册结果
     */
    @Override
    @Transactional
    public GroupDetailRegisterRespVO register(GroupRegisterReqVO reqVO) {
        log.info("开始注册网关实例，请求参数：{}", reqVO);

        Integer count = gatewayGroupDetailMapper.registerIfAbsent(reqVO.getDetailAddress());
        String groupId = gatewayGroupMapper.getIdByKey(reqVO.getGroupKey());

        if (groupId == null) {
            log.warn("注册网关实例失败：未选择所属网关组");
            throw new RuntimeException("请选择所属网关组");
        }

        GatewayServerDO server = gatewayServerMapper.getServerByGroupId(groupId);

        if (count > 0) {
            redisPubUtil.heartBeat();
            log.info("网关实例已存在，直接返回注册信息，groupName={}", server.getServerName());

            return GroupDetailRegisterRespVO.builder()
                    .serverName(server.getServerName())
                    .safeKey(server.getSafeKey())
                    .safeSecret(server.getSafeSecret())
                    .build();
        }

        String detailId = gatewayGroupDetailMapper.getIdByAddr(reqVO.getDetailAddress());
        GatewayGroupDetailDO detailDO = new GatewayGroupDetailDO();
        detailDO.setId(detailId == null ? uniqueIdUtil.nextId() : detailId);
        detailDO.setGroupId(groupId);
        detailDO.setDetailName(reqVO.getDetailName());
        detailDO.setDetailAddress(reqVO.getDetailAddress());
        detailDO.setDetailWeight(reqVO.getDetailWeight());
        detailDO.setStatus(StatusEnum.ENABLE.getValue());

        try {
            if (detailId != null) {
                gatewayGroupDetailMapper.updateById(detailDO);
            } else {
                gatewayGroupDetailMapper.insert(detailDO);
            }
            redisPubUtil.heartBeat();

            log.info("网关实例注册成功，groupName={}", server.getServerName());

            return GroupDetailRegisterRespVO.builder()
                    .serverName(server.getServerName())
                    .safeKey(server.getSafeKey())
                    .safeSecret(server.getSafeSecret())
                    .build();
        } catch (Exception e) {
            log.error("注册创建失败，错误信息：{}", e.getMessage(), e);
            throw new RuntimeException("注册创建失败");
        }
    }

    /**
     * 心跳检测（保活机制）
     *
     * @param reqVO 心跳请求参数
     * @return 服务器名称
     */
    @Override
    public String keepAlive(HeartBeatReqVO reqVO) {
        log.info("收到网关实例心跳请求，请求参数：{}", reqVO);

        String key = reqVO.getGroupKey();
        String server = getServerName(key);

        Map<Object, Object> entries = redisTemplate.opsForHash()
                .entries("heartbeat:group:" + server + ":" + reqVO.getAddr());

        if (entries.isEmpty()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("lastTime", LocalDateTime.now().toString());
            map.put("startTime", LocalDateTime.now().toString());
            map.put("url", reqVO.getAddr());
            map.put("weight", reqVO.getWeight());

            redisTemplate.opsForHash().putAll("heartbeat:group:" + server + ":" + reqVO.getAddr(), map);
            redisTemplate.expire("heartbeat:group:" + server + ":" + reqVO.getAddr(), 30, TimeUnit.SECONDS);
            return server;
        }
        redisTemplate.opsForHash().put("heartbeat:group:" + server + ":" + reqVO.getAddr(), "lastTime", LocalDateTime.now().toString());
        redisTemplate.expire("heartbeat:group:" + server + ":" + reqVO.getAddr(), 30, TimeUnit.SECONDS);
        nginxConfUtil.refreshNginxConfig();
        return server;
    }

    /**
     * 根据分组ID获取该分组下的所有实例列表
     *
     * @param groupId 分组ID
     * @return 实例列表
     */
    @Override
    public List<GroupDetailSaveReqVO> listByGroupId(String groupId) {
        log.info("开始根据分组ID获取实例列表，groupId={}", groupId);

        if (StrUtil.isBlank(groupId)) {
            log.warn("获取实例列表失败：groupId为空");
            throw new RuntimeException("分组ID不能为空");
        }

        List<GroupDetailSaveReqVO> list = gatewayGroupDetailMapper.listByGroupId(groupId);
        log.info("成功获取实例列表，groupId={}，共查询到{}条记录", groupId, list.size());

        return list;
    }
}
