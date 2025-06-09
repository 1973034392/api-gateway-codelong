package top.codelong.apigatewaycenter.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDetailDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupDetailMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.enums.StatusEnum;
import top.codelong.apigatewaycenter.service.GatewayGroupDetailService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description 针对表【gateway_group_detail(网关实例信息表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Service
@RequiredArgsConstructor
public class GatewayGroupDetailServiceImpl extends ServiceImpl<GatewayGroupDetailMapper, GatewayGroupDetailDO> implements GatewayGroupDetailService {

    private final GatewayGroupDetailMapper gatewayGroupDetailMapper;
    private final GatewayGroupMapper gatewayGroupMapper;
    private final UniqueIdUtil uniqueIdUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GatewayServerMapper gatewayServerMapper;

    @Override
    public Long create(GroupDetailSaveReqVO reqVO) {
        String key = reqVO.getGroupKey();
        if (StrUtil.isBlank(key)) {
            throw new RuntimeException("请选择所属网关实例");
        }
        Long groupId = gatewayGroupMapper.getIdByKey(key);
        if (groupId == null) {
            throw new RuntimeException("网关实例不存在");
        }
        Long id = gatewayGroupDetailMapper.getIdByAddr(reqVO.getAddress());
        if (id != null) {
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
        return gatewayGroupDetailDO.getId();
    }

    @Override
    public Boolean update(GroupDetailSaveReqVO reqVO) {
        String key = reqVO.getGroupKey();
        if (StrUtil.isBlank(key)) {
            throw new RuntimeException("请选择所属网关组");
        }
        Long groupId = gatewayGroupMapper.getIdByKey(key);
        if (groupId == null) {
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
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        if (id == null) {
            throw new RuntimeException("网关实例组详情id不能为空");
        }
        GatewayGroupDetailDO gatewayGroupDetailDO = gatewayGroupDetailMapper.selectById(id);
        if (gatewayGroupDetailDO == null) {
            throw new RuntimeException("网关实例组详情不存在");
        }
        gatewayGroupDetailMapper.deleteById(id);
        return true;
    }

    @Override
    public GroupDetailSaveReqVO get(Long id) {
        if (id == null) {
            throw new RuntimeException("网关实例组详情id不能为空");
        }
        GatewayGroupDetailDO gatewayGroupDetailDO = gatewayGroupDetailMapper.selectById(id);
        if (gatewayGroupDetailDO == null) {
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
        return groupDetailSaveReqVO;
    }

    @Override
    public Boolean updateStatus(Long id) {
        if (id == null) {
            throw new RuntimeException("网关实例组详情id不能为空");
        }
        GatewayGroupDetailDO gatewayGroupDetailDO = gatewayGroupDetailMapper.selectById(id);
        if (gatewayGroupDetailDO == null) {
            throw new RuntimeException("网关实例组详情不存在");
        }
        Integer status = gatewayGroupDetailDO.getStatus();
        if (status.equals(StatusEnum.DISABLE.getValue())) {
            gatewayGroupDetailDO.setStatus(StatusEnum.ENABLE.getValue());
        } else {
            gatewayGroupDetailDO.setStatus(StatusEnum.DISABLE.getValue());
        }
        gatewayGroupDetailMapper.updateById(gatewayGroupDetailDO);
        return true;
    }

    @Override
    public PageResult<GroupDetailSaveReqVO> page(GroupDetailPageReqVO reqVO) {
        Page<GroupDetailSaveReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        List<GroupDetailSaveReqVO> list = gatewayGroupDetailMapper.pageInfo(page, reqVO);
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public String getServerName(String groupKey) {
        if (StrUtil.isBlank(groupKey)) {
            throw new RuntimeException("请选择所属网关组");
        }
        return gatewayGroupMapper.getServerNameByGroupKey(groupKey);
    }

    @Override
    public Boolean keepAlive(HeartBeatReqVO reqVO) {
        String server = reqVO.getServer();
        Map<Object, Object> entries = redisTemplate.opsForHash()
                .entries("heartbeat:group:" + server + ":" + reqVO.getAddr());
        if (entries.isEmpty()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("lastTime", LocalDateTime.now().toString());
            map.put("startTime", LocalDateTime.now().toString());
            map.put("url", reqVO.getAddr());
            map.put("weight", 1);
            redisTemplate.opsForHash().putAll("heartbeat:group:" + server + ":" + reqVO.getAddr(), map);
            redisTemplate.expire("heartbeat:group:" + server + ":" + reqVO.getAddr(), 30, TimeUnit.SECONDS);
            return true;
        }
        redisTemplate.opsForHash().put("heartbeat:group:" + server + ":" + reqVO.getAddr(), "lastTime", LocalDateTime.now().toString());
        redisTemplate.expire("heartbeat:group:" + server + ":" + reqVO.getAddr(), 30, TimeUnit.SECONDS);
        return true;
    }
}




