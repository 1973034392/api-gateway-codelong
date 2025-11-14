package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import top.codelong.apigatewaycenter.dto.req.GroupPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupSaveReqVO;

/**
 * @author CodeLong
 * @description 针对表【gateway_group(网关实例分组表)】的数据库操作Service
 * @createDate 2025-05-23 16:05:44
 */
public interface GatewayGroupService extends IService<GatewayGroupDO> {

    String create(GroupSaveReqVO reqVO);

    Boolean update(GroupSaveReqVO reqVO);

    Boolean delete(String id);

    PageResult<GroupSaveReqVO> page(GroupPageReqVO reqVO);
}
