package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayMethodMapper;
import top.codelong.apigatewaycenter.service.GatewayMethodService;

import java.util.List;

/**
* @author CodeLong
* @description 针对表【gateway_method(方法信息表)】的数据库操作Service实现
* @createDate 2025-05-23 16:05:44
*/
@Slf4j
@Service
public class GatewayMethodServiceImpl extends ServiceImpl<GatewayMethodMapper, GatewayMethodDO>
    implements GatewayMethodService {

    /**
     * 根据接口ID获取方法列表
     * @param interfaceId 接口ID
     * @return 方法列表
     */
    @Override
    public List<GatewayMethodDO> listByInterfaceId(String interfaceId) {
        log.info("根据接口ID获取方法列表，interfaceId: {}", interfaceId);

        LambdaQueryWrapper<GatewayMethodDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GatewayMethodDO::getInterfaceId, interfaceId);
        queryWrapper.orderByDesc(GatewayMethodDO::getId);

        List<GatewayMethodDO> methods = this.list(queryWrapper);
        log.info("成功获取方法列表，总数: {}", methods.size());

        return methods;
    }
}




