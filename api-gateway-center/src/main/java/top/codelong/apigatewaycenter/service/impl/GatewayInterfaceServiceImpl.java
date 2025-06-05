package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDetailDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayInterfaceMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayMethodMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerDetailMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.domain.MethodSaveDomain;
import top.codelong.apigatewaycenter.dto.req.InterfaceMethodSaveReqVO;
import top.codelong.apigatewaycenter.enums.StatusEnum;
import top.codelong.apigatewaycenter.scheduled.InterfaceFlushScheduled;
import top.codelong.apigatewaycenter.service.GatewayInterfaceService;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【gateway_interface(接口信息表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Service
@RequiredArgsConstructor
public class GatewayInterfaceServiceImpl extends ServiceImpl<GatewayInterfaceMapper, GatewayInterfaceDO> implements GatewayInterfaceService {
    private final GatewayServerMapper gatewayServerMapper;
    private final GatewayInterfaceMapper gatewayInterfaceMapper;
    private final GatewayMethodMapper gatewayMethodMapper;
    private final GatewayServerDetailMapper gatewayServerDetailMapper;
    private final UniqueIdUtil uniqueIdUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InterfaceFlushScheduled interfaceFlushScheduled;
    private final RedisPubUtil redisPubUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InterfaceMethodSaveReqVO reqVO) {
        String safeKey = reqVO.getSafeKey();
        String safeSecret = reqVO.getSafeSecret();
        Long serverId = gatewayServerMapper.getIdBySafeKey(safeKey);
        if (serverId == null) {
            throw new RuntimeException("该服务不存在");
        }
        GatewayServerDO serverDO = gatewayServerMapper.selectById(serverId);
        if (serverDO.getStatus().equals(StatusEnum.DISABLE.getValue())) {
            throw new RuntimeException("该服务已下线");
        }
        if (!serverDO.getSafeSecret().equals(safeSecret)) {
            throw new RuntimeException("安全密钥错误");
        }
        GatewayInterfaceDO selectedOne = gatewayInterfaceMapper.selectOne(new LambdaQueryWrapper<GatewayInterfaceDO>()
                .eq(GatewayInterfaceDO::getInterfaceName, reqVO.getInterfaceName())
                .last("limit 1"));
        GatewayInterfaceDO interfaceDO = new GatewayInterfaceDO();
        interfaceDO.setId(selectedOne == null ? uniqueIdUtil.nextId() : selectedOne.getId());
        interfaceDO.setServerId(serverId);
        interfaceDO.setInterfaceName(reqVO.getInterfaceName());
        if (selectedOne == null) {
            gatewayInterfaceMapper.insert(interfaceDO);
        } else {
            gatewayInterfaceMapper.updateById(interfaceDO);
        }

        for (MethodSaveDomain method : reqVO.getMethods()) {
            GatewayMethodDO methodDO = new GatewayMethodDO();
            GatewayMethodDO gatewayMethodDO = gatewayMethodMapper.selectOne(new LambdaQueryWrapper<GatewayMethodDO>()
                    .eq(GatewayMethodDO::getMethodName, method.getMethodName())
                    .eq(GatewayMethodDO::getInterfaceId, interfaceDO.getId())
                    .eq(GatewayMethodDO::getParameterType, method.getParameterType())
                    .last("limit 1"));
            methodDO.setInterfaceId(interfaceDO.getId());
            methodDO.setMethodName(method.getMethodName());
            methodDO.setParameterType(method.getParameterType());
            methodDO.setUrl(method.getUrl());
            methodDO.setIsAuth(method.getIsAuth());
            methodDO.setIsHttp(method.getIsHttp());
            methodDO.setHttpType(method.getHttpType());
            try {
                if (gatewayMethodDO == null) {
                    methodDO.setId(uniqueIdUtil.nextId());
                    gatewayMethodMapper.insert(methodDO);
                } else {
                    methodDO.setId(gatewayMethodDO.getId());
                    gatewayMethodMapper.updateById(methodDO);
                }
            } catch (Exception e) {
                throw new RuntimeException("方法部分参数为空");
            }
        }
        new Thread(() -> {
            interfaceFlushScheduled.flushURL();
            registerMethodURL(reqVO, serverId, serverDO.getServerName());
        }).start();
        return interfaceDO.getId();
    }

    private void registerMethodURL(InterfaceMethodSaveReqVO reqVO, Long serverId, String serverName) {
        String serverUrl = reqVO.getServerUrl();
        GatewayServerDetailDO detailDO = gatewayServerDetailMapper.selectOne(new LambdaQueryWrapper<GatewayServerDetailDO>()
                .eq(GatewayServerDetailDO::getServerAddress, serverUrl)
                .last("limit 1"));
        if (detailDO == null) {
            detailDO = new GatewayServerDetailDO();
            detailDO.setId(uniqueIdUtil.nextId());
            detailDO.setStatus(StatusEnum.ENABLE.getValue());
            detailDO.setServerAddress(serverUrl);
            detailDO.setServerId(serverId);
            gatewayServerDetailMapper.insert(detailDO);
        } else {
            detailDO.setStatus(StatusEnum.ENABLE.getValue());
            detailDO.setServerId(serverId);
            gatewayServerDetailMapper.updateById(detailDO);
        }
        List<MethodSaveDomain> methods = reqVO.getMethods();
        for (MethodSaveDomain method : methods) {
            String key = "Server" + ":" + serverName + ":" + reqVO.getServerUrl();
            redisTemplate.opsForValue().set(key, method.getUrl());
        }
        redisPubUtil.publish("ServerFlush", serverName);
    }
}




