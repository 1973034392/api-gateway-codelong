package top.codelong.apigatewaycenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;
import top.codelong.apigatewaycenter.dto.req.ServerPageReqVO;
import top.codelong.apigatewaycenter.dto.req.ServerSaveReqVO;
import top.codelong.apigatewaycenter.enums.StatusEnum;
import top.codelong.apigatewaycenter.service.GatewayServerService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【gateway_server(网关系统表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Service
@RequiredArgsConstructor
public class GatewayServerServiceImpl extends ServiceImpl<GatewayServerMapper, GatewayServerDO> implements GatewayServerService {
    private final GatewayServerMapper gatewayServerMapper;
    private final UniqueIdUtil uniqueIdUtil;

    @Override
    public Long create(ServerSaveReqVO reqVO) {
        GatewayServerDO bean = BeanUtil.toBean(reqVO, GatewayServerDO.class);
        boolean isExist = gatewayServerMapper.nameIsExist(bean.getServerName());
        bean.setStatus(StatusEnum.DISABLE.getValue());
        if (isExist) {
            throw new RuntimeException("服务名称已存在");
        }
        if (StrUtil.isBlank(bean.getSafeKey())) {
            throw new RuntimeException("安全key不能为空");
        }
        if (StrUtil.isBlank(bean.getSafeSecret())) {
            throw new RuntimeException("安全密钥不能为空");
        }
        try {
            bean.setId(uniqueIdUtil.nextId());
            gatewayServerMapper.insert(bean);
        } catch (Exception e) {
            throw new RuntimeException("创建失败");
        }
        return bean.getId();
    }

    @Override
    public boolean update(ServerSaveReqVO reqVO) {
        GatewayServerDO bean = BeanUtil.toBean(reqVO, GatewayServerDO.class);
        boolean isExist = gatewayServerMapper.nameIsExist(bean.getServerName());
        if (isExist) {
            throw new RuntimeException("服务名称已存在");
        }
        if (StrUtil.isBlank(bean.getSafeKey())) {
            throw new RuntimeException("安全key不能为空");
        }
        if (StrUtil.isBlank(bean.getSafeSecret())) {
            throw new RuntimeException("安全密钥不能为空");
        }
        try {
            gatewayServerMapper.updateById(bean);
        } catch (Exception e) {
            throw new RuntimeException("更新失败");
        }
        return true;
    }

    @Override
    public Boolean updateStatus(Long id) {
        GatewayServerDO serverDO = gatewayServerMapper.selectById(id);
        if (serverDO == null) {
            throw new RuntimeException("服务不存在");
        }
        if (serverDO.getStatus().equals(StatusEnum.ENABLE.getValue())) {
            serverDO.setStatus(StatusEnum.DISABLE.getValue());
        } else if (serverDO.getStatus().equals(StatusEnum.DISABLE.getValue())) {
            serverDO.setStatus(StatusEnum.ENABLE.getValue());
        }
        return gatewayServerMapper.updateById(serverDO) > 0;
    }

    @Override
    public ServerSaveReqVO get(Long id) {
        GatewayServerDO serverDO = gatewayServerMapper.selectById(id);
        if (serverDO == null) {
            throw new RuntimeException("服务不存在");
        }
        return BeanUtil.toBean(serverDO, ServerSaveReqVO.class);
    }

    @Override
    public PageResult<GatewayServerDO> page(ServerPageReqVO reqVO) {
        Page<ServerSaveReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        List<GatewayServerDO> list = gatewayServerMapper.pageInfo(page, reqVO.getServerName(), reqVO.getStatus(), reqVO.getNginxAddr());
        return new PageResult<>(list, page.getTotal());
    }
}




