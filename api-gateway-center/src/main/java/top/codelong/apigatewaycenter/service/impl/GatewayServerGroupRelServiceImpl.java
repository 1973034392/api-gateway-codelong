package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerGroupRelMapper;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayServerGroupRelService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

/**
 * 网关服务与分组关联服务实现类
 * @author CodeLong
 * @description 针对表【gateway_server_group_rel(网关系统和实例分组关联表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayServerGroupRelServiceImpl extends ServiceImpl<GatewayServerGroupRelMapper, GatewayServerGroupRelDO> implements GatewayServerGroupRelService {
    private final GatewayServerGroupRelMapper gatewayServerGroupRelMapper;
    private final UniqueIdUtil uniqueIdUtil;

    /**
     * 创建网关服务与分组的关联关系
     * @param reqVO 关联关系保存请求VO
     * @return 创建成功的关联ID
     * @throws RuntimeException 当组或系统已被绑定时抛出
     */
    @Override
    public Long create(ServerGroupRelSaveReqVO reqVO) {
        log.info("开始创建网关服务与分组关联关系，serverId: {}, groupId: {}",
                reqVO.getServerId(), reqVO.getGroupId());

        Long serverId = reqVO.getServerId();
        Long groupId = reqVO.getGroupId();

        // 检查分组是否已被绑定
        boolean groupExist = gatewayServerGroupRelMapper.exists(new LambdaQueryWrapper<GatewayServerGroupRelDO>()
                .eq(GatewayServerGroupRelDO::getGroupId, groupId));
        // 检查服务是否已被绑定
        boolean serverExist = gatewayServerGroupRelMapper.exists(new LambdaQueryWrapper<GatewayServerGroupRelDO>()
                .eq(GatewayServerGroupRelDO::getServerId, serverId));

        if (groupExist || serverExist) {
            log.error("创建关联关系失败，组或系统已被绑定，serverId: {}, groupId: {}", serverId, groupId);
            throw new RuntimeException("该组或该系统已被绑定");
        }

        GatewayServerGroupRelDO relDO = new GatewayServerGroupRelDO();
        relDO.setId(uniqueIdUtil.nextId());
        relDO.setServerId(serverId);
        relDO.setGroupId(groupId);

        gatewayServerGroupRelMapper.insert(relDO);
        log.info("成功创建网关服务与分组关联关系，relId: {}", relDO.getId());

        return relDO.getId();
    }
}