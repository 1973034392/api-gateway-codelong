package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDetailDO;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.dto.req.ServerDetailRegisterReqVO;

/**
* @author CodeLong
* @description 针对表【gateway_server_detail(系统详细信息表)】的数据库操作Service
* @createDate 2025-05-23 16:05:44
*/
public interface GatewayServerDetailService extends IService<GatewayServerDetailDO> {

    Boolean register(ServerDetailRegisterReqVO reqVO);

    Boolean offline(Long id);

    Boolean keepAlive(HeartBeatReqVO reqVO);
}
