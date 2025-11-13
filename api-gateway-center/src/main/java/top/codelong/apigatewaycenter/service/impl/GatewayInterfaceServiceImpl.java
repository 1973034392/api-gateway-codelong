package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.codelong.apigatewaycenter.common.page.PageResult;
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
import top.codelong.apigatewaycenter.service.GatewayInterfaceService;
import top.codelong.apigatewaycenter.utils.RedisPubUtil;
import top.codelong.apigatewaycenter.utils.UniqueIdUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网关接口服务实现类
 *
 * @author CodeLong
 * @description 针对表【gateway_interface(接口信息表)】的数据库操作Service实现
 * @createDate 2025-05-23 16:05:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatewayInterfaceServiceImpl extends ServiceImpl<GatewayInterfaceMapper, GatewayInterfaceDO> implements GatewayInterfaceService {
    private final GatewayServerMapper gatewayServerMapper;
    private final GatewayInterfaceMapper gatewayInterfaceMapper;
    private final GatewayMethodMapper gatewayMethodMapper;
    private final GatewayServerDetailMapper gatewayServerDetailMapper;
    private final UniqueIdUtil uniqueIdUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisPubUtil redisPubUtil;

    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    /**
     * 创建接口及其方法
     *
     * @param reqVO 接口方法保存请求VO
     * @return 创建的接口ID
     * @throws RuntimeException 当服务不存在/已下线/安全密钥错误/方法参数为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(InterfaceMethodSaveReqVO reqVO) {
        log.info("开始创建接口及其方法，请求参数: {}", reqVO);

        String safeKey = reqVO.getSafeKey();
        String safeSecret = reqVO.getSafeSecret();
        Long serverId = gatewayServerMapper.getIdBySafeKey(safeKey);
        if (serverId == null) {
            log.error("创建接口失败，服务不存在，safeKey: {}", safeKey);
            throw new RuntimeException("该服务不存在");
        }

        GatewayServerDO serverDO = gatewayServerMapper.selectById(serverId);
        if (serverDO.getStatus().equals(StatusEnum.DISABLE.getValue())) {
            log.error("创建接口失败，服务已下线，serverId: {}", serverId);
            throw new RuntimeException("该服务已下线");
        }
        if (!serverDO.getSafeSecret().equals(safeSecret)) {
            log.error("创建接口失败，安全密钥错误，serverId: {}", serverId);
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
            log.debug("新增接口信息，interfaceName: {}", reqVO.getInterfaceName());
            gatewayInterfaceMapper.insert(interfaceDO);
        } else {
            log.debug("更新接口信息，interfaceId: {}", interfaceDO.getId());
            gatewayInterfaceMapper.updateById(interfaceDO);
        }

        // 处理方法信息
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
                    log.debug("新增方法信息，methodName: {}, interfaceId: {}", method.getMethodName(), interfaceDO.getId());
                    gatewayMethodMapper.insert(methodDO);
                } else {
                    methodDO.setId(gatewayMethodDO.getId());
                    log.debug("更新方法信息，methodId: {}", methodDO.getId());
                    gatewayMethodMapper.updateById(methodDO);
                }
            } catch (Exception e) {
                log.error("方法参数为空，interfaceId: {}, method: {}", interfaceDO.getId(), method);
                throw new RuntimeException("方法部分参数为空");
            }
        }

        // 异步注册服务和方法
        executor.submit(() -> {
            log.info("开始异步注册服务和方法，serverId: {}, interfaceId: {}", serverId, interfaceDO.getId());
            registerMethod(serverDO.getServerName(), interfaceDO.getInterfaceName(), reqVO.getMethods());
            registerService(reqVO, serverId);
        });

        log.info("成功创建接口及其方法，interfaceId: {}", interfaceDO.getId());
        return interfaceDO.getId();
    }

    /**
     * 注册方法到Redis缓存
     *
     * @param serverName    服务名称
     * @param interfaceName 接口名称
     * @param methods       方法列表
     */
    private void registerMethod(String serverName, String interfaceName, List<MethodSaveDomain> methods) {
        log.debug("开始注册方法到Redis，serverName: {}, interfaceName: {}", serverName, interfaceName);
        for (MethodSaveDomain method : methods) {
            Map<String, Object> params = new HashMap<>();
            params.put("interfaceName", interfaceName);
            params.put("methodName", method.getMethodName());
            params.put("parameterType", method.getParameterType());
            params.put("isAuth", method.getIsAuth());
            params.put("isHttp", method.getIsHttp());
            params.put("httpType", method.getHttpType());

            String redisKey = "URL:" + serverName + ":" + method.getUrl();
            redisTemplate.opsForHash().putAll(redisKey, params);
            log.debug("成功注册方法到Redis，key: {}", redisKey);
        }
    }

    /**
     * 注册服务到Redis缓存
     *
     * @param reqVO    接口方法保存请求VO
     * @param serverId 服务ID
     */
    private void registerService(InterfaceMethodSaveReqVO reqVO, Long serverId) {
        log.debug("开始注册服务到Redis，serverId: {}", serverId);
        String serverUrl = reqVO.getServerUrl();
        GatewayServerDetailDO detailDO = gatewayServerDetailMapper.selectOne(new LambdaQueryWrapper<GatewayServerDetailDO>()
                .eq(GatewayServerDetailDO::getServerAddress, serverUrl)
                .last("limit 1"));

        if (detailDO == null) {
            log.debug("新增服务详情，serverUrl: {}", serverUrl);
            detailDO = new GatewayServerDetailDO();
            detailDO.setId(uniqueIdUtil.nextId());
            detailDO.setStatus(StatusEnum.ENABLE.getValue());
            detailDO.setServerAddress(serverUrl);
            detailDO.setServerId(serverId);
            gatewayServerDetailMapper.insert(detailDO);
        } else {
            log.debug("更新服务详情，detailId: {}", detailDO.getId());
            detailDO.setStatus(StatusEnum.ENABLE.getValue());
            detailDO.setServerId(serverId);
            gatewayServerDetailMapper.updateById(detailDO);
        }

        redisPubUtil.ServerFlush();
        redisPubUtil.heartBeat();
        log.debug("成功注册服务到Redis并发送心跳，serverId: {}", serverId);
    }

    /**
     * 分页查询接口列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param serverId 服务ID（可选）
     * @return 分页结果
     */
    @Override
    public PageResult<GatewayInterfaceDO> page(Integer pageNum, Integer pageSize, Long serverId) {
        log.info("分页查询接口列表，pageNum: {}, pageSize: {}, serverId: {}", pageNum, pageSize, serverId);

        Page<GatewayInterfaceDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<GatewayInterfaceDO> queryWrapper = new LambdaQueryWrapper<>();

        if (serverId != null) {
            queryWrapper.eq(GatewayInterfaceDO::getServerId, serverId);
        }

        queryWrapper.orderByDesc(GatewayInterfaceDO::getId);

        Page<GatewayInterfaceDO> resultPage = this.page(page, queryWrapper);

        log.info("成功查询接口列表，总数: {}", resultPage.getTotal());
        return new PageResult<>(resultPage.getRecords(), resultPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 获取所有接口列表
     * @return 接口列表
     */
    @Override
    public List<GatewayInterfaceDO> list() {
        log.info("获取所有接口列表");

        LambdaQueryWrapper<GatewayInterfaceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(GatewayInterfaceDO::getId);

        List<GatewayInterfaceDO> list = this.list(queryWrapper);

        log.info("成功获取接口列表，总数: {}", list.size());
        return list;
    }
}