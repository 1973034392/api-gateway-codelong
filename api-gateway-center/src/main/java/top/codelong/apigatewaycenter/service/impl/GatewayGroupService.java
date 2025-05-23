package top.codelong.apigatewaycenter.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import top.codelong.apigatewaycenter.dto.req.GroupPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupSaveReqVO;

/**
 * @author Administrator
 * @description 针对表【gateway_group(网关实例分组表)】的数据库操作Service
 * @createDate 2025-05-23 16:05:44
 */
public interface GatewayGroupService extends IService<GatewayGroupDO> {

    Long create(GroupSaveReqVO reqVO);

    Boolean update(GroupSaveReqVO reqVO);

    Boolean delete(Long id);

    PageResult<GroupSaveReqVO> page(GroupPageReqVO reqVO);
}
