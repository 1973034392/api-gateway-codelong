package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import top.codelong.apigatewaycenter.dto.req.InterfaceMethodSaveReqVO;

/**
* @author Administrator
* @description 针对表【gateway_interface(接口信息表)】的数据库操作Service
* @createDate 2025-05-23 16:05:44
*/
public interface GatewayInterfaceService extends IService<GatewayInterfaceDO> {

    Long create(InterfaceMethodSaveReqVO reqVO);
}
