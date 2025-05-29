package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerGroupRelMapper;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayServerGroupRelService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

/**
 * @author Administrator
 * @description 针对表【gateway_server_group_rel(网关系统和实例分组关联表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Service
@RequiredArgsConstructor
public class GatewayServerGroupRelServiceImpl extends ServiceImpl<GatewayServerGroupRelMapper, GatewayServerGroupRelDO> implements GatewayServerGroupRelService {
    private final GatewayServerGroupRelMapper gatewayServerGroupRelMapper;
    private final UniqueIdUtil uniqueIdUtil;

    @Override
    public Long create(ServerGroupRelSaveReqVO reqVO) {
        Long serverId = reqVO.getServerId();
        Long groupId = reqVO.getGroupId();
        boolean groupExist = gatewayServerGroupRelMapper.exists(new LambdaQueryWrapper<GatewayServerGroupRelDO>()
                .eq(GatewayServerGroupRelDO::getGroupId, groupId));
        boolean serverExist = gatewayServerGroupRelMapper.exists(new LambdaQueryWrapper<GatewayServerGroupRelDO>()
                .eq(GatewayServerGroupRelDO::getServerId, serverId));

        if (groupExist || serverExist) {
            throw new RuntimeException("该组或该系统已被绑定");
        }
        GatewayServerGroupRelDO relDO = new GatewayServerGroupRelDO();
        relDO.setId(uniqueIdUtil.nextId());
        relDO.setServerId(serverId);
        relDO.setGroupId(groupId);
        gatewayServerGroupRelMapper.insert(relDO);
        return relDO.getId();
    }
}




