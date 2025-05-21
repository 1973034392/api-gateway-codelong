package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import top.codelong.apigatewaycenter.service.GatewayInterfaceService;
import top.codelong.apigatewaycenter.dao.mapper.GatewayInterfaceMapper;
import org.springframework.stereotype.Service;

/**
* @author codelong
* @description 针对表【gateway_interface(接口信息表)】的数据库操作Service实现
* @createDate 2025-05-21 18:07:52
*/
@Service
public class GatewayInterfaceServiceImpl extends ServiceImpl<GatewayInterfaceMapper, GatewayInterfaceDO>
    implements GatewayInterfaceService{

}




