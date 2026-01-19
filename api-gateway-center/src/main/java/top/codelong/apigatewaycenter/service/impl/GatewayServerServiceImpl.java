package top.codelong.apigatewaycenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 网关服务实现类
 *
 * @author CodeLong
 * @description 针对表【gateway_server(网关系统表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayServerServiceImpl extends ServiceImpl<GatewayServerMapper, GatewayServerDO> implements GatewayServerService {
    private final GatewayServerMapper gatewayServerMapper;
    private final UniqueIdUtil uniqueIdUtil;

    /**
     * 创建网关服务
     *
     * @param reqVO 服务保存请求VO
     * @return 创建的服务ID
     * @throws RuntimeException 当服务名称已存在或安全信息为空时抛出
     */
    @Override
    public String create(ServerSaveReqVO reqVO) {
        log.info("开始创建网关服务，请求参数: {}", reqVO);

        GatewayServerDO bean = BeanUtil.toBean(reqVO, GatewayServerDO.class);
        boolean isExist = gatewayServerMapper.nameIsExist(bean.getServerName());
        bean.setStatus(StatusEnum.ENABLE.getValue());

        if (isExist) {
            log.error("创建网关服务失败，服务名称已存在: {}", bean.getServerName());
            throw new RuntimeException("服务名称已存在");
        }
        if (StrUtil.isBlank(bean.getSafeKey())) {
            log.error("创建网关服务失败，安全key不能为空");
            throw new RuntimeException("安全key不能为空");
        }
        if (StrUtil.isBlank(bean.getSafeSecret())) {
            log.error("创建网关服务失败，安全密钥不能为空");
            throw new RuntimeException("安全密钥不能为空");
        }

        try {
            bean.setId(uniqueIdUtil.nextId());
            gatewayServerMapper.insert(bean);
            log.info("成功创建网关服务，服务ID: {}", bean.getId());
        } catch (Exception e) {
            log.error("创建网关服务失败", e);
            throw new RuntimeException("创建失败");
        }
        return bean.getId();
    }

    /**
     * 更新网关服务信息
     *
     * @param reqVO 服务保存请求VO
     * @return 更新是否成功
     * @throws RuntimeException 当服务名称已存在或安全信息为空时抛出
     */
    @Override
    public boolean update(ServerSaveReqVO reqVO) {
        log.info("开始更新网关服务，服务ID: {}", reqVO.getId());

        GatewayServerDO bean = BeanUtil.toBean(reqVO, GatewayServerDO.class);
        boolean isExist = gatewayServerMapper.nameIsExist(bean.getServerName());

        if (isExist) {
            log.error("更新网关服务失败，服务名称已存在: {}", bean.getServerName());
            throw new RuntimeException("服务名称已存在");
        }
        if (StrUtil.isBlank(bean.getSafeKey())) {
            log.error("更新网关服务失败，安全key不能为空");
            throw new RuntimeException("安全key不能为空");
        }
        if (StrUtil.isBlank(bean.getSafeSecret())) {
            log.error("更新网关服务失败，安全密钥不能为空");
            throw new RuntimeException("安全密钥不能为空");
        }

        try {
            gatewayServerMapper.updateById(bean);
            log.info("成功更新网关服务，服务ID: {}", bean.getId());
        } catch (Exception e) {
            log.error("更新网关服务失败，服务ID: {}", reqVO.getId(), e);
            throw new RuntimeException("更新失败");
        }
        return true;
    }

    /**
     * 更新服务状态
     *
     * @param id 服务ID
     * @return 更新是否成功
     * @throws RuntimeException 当服务不存在时抛出
     */
    @Override
    public Boolean updateStatus(String id) {
        log.info("开始更新服务状态，服务ID: {}", id);

        GatewayServerDO serverDO = gatewayServerMapper.selectById(id);
        if (serverDO == null) {
            log.error("更新服务状态失败，服务不存在，ID: {}", id);
            throw new RuntimeException("服务不存在");
        }

        if (serverDO.getStatus().equals(StatusEnum.ENABLE.getValue())) {
            serverDO.setStatus(StatusEnum.DISABLE.getValue());
            log.debug("将服务状态从ENABLE改为DISABLE，服务ID: {}", id);
        } else if (serverDO.getStatus().equals(StatusEnum.DISABLE.getValue())) {
            serverDO.setStatus(StatusEnum.ENABLE.getValue());
            log.debug("将服务状态从DISABLE改为ENABLE，服务ID: {}", id);
        }

        boolean result = gatewayServerMapper.updateById(serverDO) > 0;
        log.info("服务状态更新{}，服务ID: {}", result ? "成功" : "失败", id);
        return result;
    }

    /**
     * 获取服务详情
     *
     * @param id 服务ID
     * @return 服务详情VO
     * @throws RuntimeException 当服务不存在时抛出
     */
    @Override
    public ServerSaveReqVO get(String id) {
        log.info("获取服务详情，服务ID: {}", id);

        GatewayServerDO serverDO = gatewayServerMapper.selectById(id);
        if (serverDO == null) {
            log.error("获取服务详情失败，服务不存在，ID: {}", id);
            throw new RuntimeException("服务不存在");
        }

        ServerSaveReqVO result = BeanUtil.toBean(serverDO, ServerSaveReqVO.class);
        log.debug("成功获取服务详情，服务ID: {}", id);
        return result;
    }

    /**
     * 分页查询服务列表
     *
     * @param reqVO 分页查询请求VO
     * @return 分页结果
     */
    @Override
    public PageResult<GatewayServerDO> page(ServerPageReqVO reqVO) {
        log.info("分页查询服务列表，参数: 服务名称={}, 状态={}",
                reqVO.getServerName(), reqVO.getStatus());

        Page<ServerSaveReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        List<GatewayServerDO> list = gatewayServerMapper.pageInfo(
                page, reqVO.getServerName(), reqVO.getStatus());

        log.info("成功查询服务列表，总数: {}", page.getTotal());
        return new PageResult<>(list, page.getTotal());
    }
}