package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerGroupRelMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;
import top.codelong.apigatewaycenter.dto.resp.ServerGroupRelRespVO;
import top.codelong.apigatewaycenter.service.GatewayServerGroupRelService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.util.List;
import java.util.stream.Collectors;

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
    private final GatewayServerMapper gatewayServerMapper;
    private final GatewayGroupMapper gatewayGroupMapper;
    private final UniqueIdUtil uniqueIdUtil;

    /**
     * 创建网关服务与分组的关联关系
     * @param reqVO 关联关系保存请求VO
     * @return 创建成功的关联ID
     * @throws RuntimeException 当组或系统已被绑定时抛出
     */
    @Override
    public String create(ServerGroupRelSaveReqVO reqVO) {
        log.info("开始创建网关服务与分组关联关系，serverId: {}, groupId: {}",
                reqVO.getServerId(), reqVO.getGroupId());

        String serverId = reqVO.getServerId();
        String groupId = reqVO.getGroupId();

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

    /**
     * 分页查询服务与分组关联列表
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ServerGroupRelRespVO> page(Integer pageNo, Integer pageSize) {
        log.info("分页查询服务与分组关联列表，pageNo: {}, pageSize: {}", pageNo, pageSize);

        Page<GatewayServerGroupRelDO> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<GatewayServerGroupRelDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(GatewayServerGroupRelDO::getId);

        Page<GatewayServerGroupRelDO> resultPage = this.page(page, queryWrapper);

        // 转换为响应VO,关联查询服务和分组信息
        List<ServerGroupRelRespVO> respList = resultPage.getRecords().stream().map(rel -> {
            // 查询核心服务信息
            GatewayServerDO server = gatewayServerMapper.selectById(rel.getServerId());
            // 查询网关分组信息
            GatewayGroupDO group = gatewayGroupMapper.selectById(rel.getGroupId());

            return ServerGroupRelRespVO.builder()
                    .id(rel.getId())
                    .serverId(rel.getServerId())
                    .serverName(server != null ? server.getServerName() : "")
                    .serverKey(server != null ? server.getSafeKey() : "")
                    .groupId(rel.getGroupId())
                    .groupName(group != null ? group.getGroupName() : "")
                    .groupKey(group != null ? group.getGroupKey() : "")
                    .createTime(rel.getCreateTime())
                    .updateTime(rel.getUpdateTime())
                    .build();
        }).collect(Collectors.toList());

        log.info("成功查询服务与分组关联列表，总数: {}", resultPage.getTotal());
        return new PageResult<>(respList, resultPage.getTotal(), pageNo, pageSize);
    }
}