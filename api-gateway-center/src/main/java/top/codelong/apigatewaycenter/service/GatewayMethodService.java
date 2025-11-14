package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;

import java.util.List;

/**
* @author CodeLong
* @description 针对表【gateway_method(方法信息表)】的数据库操作Service
* @createDate 2025-05-23 16:05:44
*/
public interface GatewayMethodService extends IService<GatewayMethodDO> {

    /**
     * 根据接口ID获取方法列表
     * @param interfaceId 接口ID
     * @return 方法列表
     */
    List<GatewayMethodDO> listByInterfaceId(String interfaceId);
}
