package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import top.codelong.apigatewaycenter.dto.req.ServerGroupRelSaveReqVO;
import top.codelong.apigatewaycenter.dto.resp.ServerGroupRelRespVO;

/**
* @author CodeLong
* @description 针对表【gateway_server_group_rel(网关系统和实例分组关联表)】的数据库操作Service
* @createDate 2025-05-23 16:05:44
*/
public interface GatewayServerGroupRelService extends IService<GatewayServerGroupRelDO> {

    String create(ServerGroupRelSaveReqVO reqVO);

    /**
     * 分页查询服务与分组关联列表
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<ServerGroupRelRespVO> page(Integer pageNo, Integer pageSize);
}
