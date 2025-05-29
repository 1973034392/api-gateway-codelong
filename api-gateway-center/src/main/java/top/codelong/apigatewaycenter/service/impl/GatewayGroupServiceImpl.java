package top.codelong.apigatewaycenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayGroupMapper;
import top.codelong.apigatewaycenter.dto.req.GroupPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupSaveReqVO;
import top.codelong.apigatewaycenter.service.GatewayGroupService;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【gateway_group(网关实例分组表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Service
@RequiredArgsConstructor
public class GatewayGroupServiceImpl extends ServiceImpl<GatewayGroupMapper, GatewayGroupDO> implements GatewayGroupService {

    private final GatewayGroupMapper gatewayGroupMapper;
    private final UniqueIdUtil uniqueIdUtil;

    @Override
    public Long create(GroupSaveReqVO reqVO) {
        GatewayGroupDO bean = BeanUtil.toBean(reqVO, GatewayGroupDO.class);
        bean.setId(uniqueIdUtil.nextId());
        Long keyId = gatewayGroupMapper.getIdByKey(bean.getGroupKey());
        if (keyId != null) {
            throw new RuntimeException("网关实例分组key已存在");
        }
        gatewayGroupMapper.insert(bean);
        return bean.getId();
    }

    @Override
    public Boolean update(GroupSaveReqVO reqVO) {
        GatewayGroupDO bean = BeanUtil.toBean(reqVO, GatewayGroupDO.class);
        bean.setGroupKey(null);
        gatewayGroupMapper.updateById(bean);
        return true;
    }

    @Override
    public Boolean delete(Long id) {
        if (id == null) {
            throw new RuntimeException("网关实例分组id不能为空");
        }
        GatewayGroupDO gatewayGroupDO = gatewayGroupMapper.selectById(id);
        if (gatewayGroupDO == null) {
            throw new RuntimeException("网关实例分组不存在");
        }
        gatewayGroupMapper.deleteById(id);
        return true;
    }

    @Override
    public PageResult<GroupSaveReqVO> page(GroupPageReqVO reqVO) {
        Page<GroupSaveReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        List<GroupSaveReqVO> list = gatewayGroupMapper.pageInfo(page, reqVO);
        return new PageResult<>(list, page.getTotal());
    }
}




