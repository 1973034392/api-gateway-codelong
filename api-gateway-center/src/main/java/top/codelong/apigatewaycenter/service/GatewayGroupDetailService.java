package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDetailDO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupRegisterReqVO;
import top.codelong.apigatewaycenter.dto.req.HeartBeatReqVO;

/**
 * @author Administrator
 * @description 针对表【gateway_group_detail(网关实例信息表)】的数据库操作Service
 * @createDate 2025-05-23 16:05:44
 */
public interface GatewayGroupDetailService extends IService<GatewayGroupDetailDO> {

    Long create(GroupDetailSaveReqVO reqVO);

    Boolean update(GroupDetailSaveReqVO reqVO);

    Boolean delete(Long id);

    GroupDetailSaveReqVO get(Long id);

    Boolean updateStatus(Long id);

    PageResult<GroupDetailSaveReqVO> page(GroupDetailPageReqVO reqVO);

    String register(GroupRegisterReqVO reqVO);

    String keepAlive(HeartBeatReqVO reqVO);

    String getServerName(String groupKey);
}
