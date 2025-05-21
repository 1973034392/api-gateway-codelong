package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import top.codelong.apigatewaycenter.service.GatewayServerGroupRelService;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerGroupRelMapper;
import org.springframework.stereotype.Service;

/**
* @author codelong
* @description 针对表【gateway_server_group_rel(网关系统和实例分组关联表)】的数据库操作Service实现
* @createDate 2025-05-21 18:07:52
*/
@Service
public class GatewayServerGroupRelServiceImpl extends ServiceImpl<GatewayServerGroupRelMapper, GatewayServerGroupRelDO>
    implements GatewayServerGroupRelService{

}




