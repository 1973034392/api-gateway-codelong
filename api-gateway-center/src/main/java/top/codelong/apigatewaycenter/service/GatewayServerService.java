package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dto.req.ServerPageReqVO;
import top.codelong.apigatewaycenter.dto.req.ServerSaveReqVO;

/**
 * @author CodeLong
 * @description 针对表【gateway_server(网关系统表)】的数据库操作Service
 * @createDate 2025-05-23 16:05:44
 */
public interface GatewayServerService extends IService<GatewayServerDO> {

    String create(ServerSaveReqVO reqVO);

    boolean update(ServerSaveReqVO reqVO);

    Boolean updateStatus(String id);

    ServerSaveReqVO get(String id);

    PageResult<GatewayServerDO> page(ServerPageReqVO reqVO);
}
