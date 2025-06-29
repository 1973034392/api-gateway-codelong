package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;

/**
* @author CodeLong
* @description 针对表【gateway_server_group_rel(网关系统和实例分组关联表)】的数据库操作Service
* @createDate 2025-05-23 16:05:44
*/
public interface GatewayServerGroupRelService extends IService<GatewayServerGroupRelDO> {

    Long create(ServerGroupRelSaveReqVO reqVO);
}
