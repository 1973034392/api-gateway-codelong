package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayInterfaceMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayMethodMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.domain.MethodSaveDomain;
import top.codelong.apigatewaycenter.dto.req.InterfaceMethodSaveReqVO;
import top.codelong.apigatewaycenter.enums.StatusEnum;
import top.codelong.apigatewaycenter.service.GatewayInterfaceService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

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
    private final UniqueIdUtil uniqueIdUtil;

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
        Long interfaceId = gatewayInterfaceMapper.selectOne(new LambdaQueryWrapper<GatewayInterfaceDO>()
                .eq(GatewayInterfaceDO::getInterfaceName, reqVO.getInterfaceName())
                .last("limit 1")).getId();
        GatewayInterfaceDO interfaceDO = new GatewayInterfaceDO();
        interfaceDO.setId(interfaceId == null ? uniqueIdUtil.nextId() : interfaceId);
        interfaceDO.setServerId(serverId);
        interfaceDO.setInterfaceName(reqVO.getInterfaceName());
        if (interfaceId == null) {
            gatewayInterfaceMapper.insert(interfaceDO);
        } else {
            gatewayInterfaceMapper.updateById(interfaceDO);
        }

        for (MethodSaveDomain method : reqVO.getMethods()) {
            GatewayMethodDO methodDO = new GatewayMethodDO();
            Long methodId = gatewayMethodMapper.selectOne(new LambdaQueryWrapper<GatewayMethodDO>()
                    .eq(GatewayMethodDO::getMethodName, method.getMethodName())
                    .last("limit 1")).getId();
            methodDO.setInterfaceId(interfaceDO.getId());
            methodDO.setMethodName(method.getMethodName());
            methodDO.setParameterType(method.getParameterType());
            methodDO.setUrl(method.getUrl());
            methodDO.setIsAuth(method.getIsAuth());
            methodDO.setIsHttp(method.getIsHttp());
            methodDO.setHttpType(method.getHttpType());
            try {
                if (methodId == null) {
                    methodDO.setId(uniqueIdUtil.nextId());
                    gatewayMethodMapper.insert(methodDO);
                } else {
                    methodDO.setId(methodId);
                    gatewayMethodMapper.updateById(methodDO);
                }
            } catch (Exception e) {
                throw new RuntimeException("方法部分参数为空");
            }
        }
        return interfaceDO.getId();
    }
}




