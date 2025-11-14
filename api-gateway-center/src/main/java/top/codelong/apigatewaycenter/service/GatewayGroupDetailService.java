package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDetailDO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupRegisterReqVO;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;
import top.codelong.apigatewaycenter.dto.resp.GroupDetailRegisterRespVO;

/**
 * @author CodeLong
 * @description 针对表【gateway_group_detail(网关实例信息表)】的数据库操作Service
 * @createDate 2025-05-23 16:05:44
 */
public interface GatewayGroupDetailService extends IService<GatewayGroupDetailDO> {

    String create(GroupDetailSaveReqVO reqVO);

    Boolean update(GroupDetailSaveReqVO reqVO);

    Boolean delete(String id);

    GroupDetailSaveReqVO get(String id);

    Boolean updateStatus(String id);

    PageResult<GroupDetailSaveReqVO> page(GroupDetailPageReqVO reqVO);

    GroupDetailRegisterRespVO register(GroupRegisterReqVO reqVO);

    String keepAlive(HeartBeatReqVO reqVO);

    String getServerName(String groupKey);

    java.util.List<GroupDetailSaveReqVO> listByGroupId(String groupId);
}
