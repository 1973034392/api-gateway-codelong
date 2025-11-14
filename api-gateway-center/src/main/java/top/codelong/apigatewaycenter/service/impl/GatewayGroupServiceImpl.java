package top.codelong.apigatewaycenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 网关实例分组服务实现类
 * @author CodeLong
 * @description 针对表【gateway_group(网关实例分组表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayGroupServiceImpl extends ServiceImpl<GatewayGroupMapper, GatewayGroupDO> implements GatewayGroupService {

    private final GatewayGroupMapper gatewayGroupMapper;
    private final UniqueIdUtil uniqueIdUtil;

    /**
     * 创建网关实例分组
     * @param reqVO 分组保存请求VO
     * @return 创建的分组ID
     * @throws RuntimeException 当分组key已存在时抛出
     */
    @Override
    public String create(GroupSaveReqVO reqVO) {
        log.info("开始创建网关实例分组，请求参数: {}", reqVO);
        GatewayGroupDO bean = BeanUtil.toBean(reqVO, GatewayGroupDO.class);
        bean.setId(uniqueIdUtil.nextId());
        String keyId = gatewayGroupMapper.getIdByKey(bean.getGroupKey());
        if (keyId != null) {
            log.error("创建网关实例分组失败，分组key已存在: {}", bean.getGroupKey());
            throw new RuntimeException("网关实例分组key已存在");
        }
        gatewayGroupMapper.insert(bean);
        log.info("成功创建网关实例分组，分组ID: {}", bean.getId());
        return bean.getId();
    }

    /**
     * 更新网关实例分组信息
     * @param reqVO 分组保存请求VO
     * @return 更新是否成功
     */
    @Override
    public Boolean update(GroupSaveReqVO reqVO) {
        log.info("开始更新网关实例分组，请求参数: {}", reqVO);
        GatewayGroupDO bean = BeanUtil.toBean(reqVO, GatewayGroupDO.class);
        bean.setGroupKey(null);
        gatewayGroupMapper.updateById(bean);
        log.info("成功更新网关实例分组，分组ID: {}", bean.getId());
        return true;
    }

    /**
     * 删除网关实例分组
     * @param id 分组ID
     * @return 删除是否成功
     * @throws RuntimeException 当ID为空或分组不存在时抛出
     */
    @Override
    public Boolean delete(String id) {
        log.info("开始删除网关实例分组，分组ID: {}", id);
        if (id == null) {
            log.error("删除网关实例分组失败，分组ID不能为空");
            throw new RuntimeException("网关实例分组id不能为空");
        }
        GatewayGroupDO gatewayGroupDO = gatewayGroupMapper.selectById(id);
        if (gatewayGroupDO == null) {
            log.error("删除网关实例分组失败，分组不存在，ID: {}", id);
            throw new RuntimeException("网关实例分组不存在");
        }
        gatewayGroupMapper.deleteById(id);
        log.info("成功删除网关实例分组，分组ID: {}", id);
        return true;
    }

    /**
     * 分页查询网关实例分组
     * @param reqVO 分页查询请求VO
     * @return 分页结果
     */
    @Override
    public PageResult<GroupSaveReqVO> page(GroupPageReqVO reqVO) {
        log.info("开始分页查询网关实例分组，请求参数: {}", reqVO);
        Page<GroupSaveReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        List<GroupSaveReqVO> list = gatewayGroupMapper.pageInfo(page, reqVO);
        log.info("成功查询网关实例分组，总数: {}", page.getTotal());
        return new PageResult<>(list, page.getTotal());
    }
}