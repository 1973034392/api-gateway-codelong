package top.codelong.apigatewaycenter.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupSaveReqVO;

import java.util.List;

/**
 * @author CodeLong
 * @description 针对表【gateway_group(网关实例分组表)】的数据库操作Mapper
 * @createDate 2025-05-23 16:05:44
 * @Entity top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO
 */
@Mapper
public interface GatewayGroupMapper extends BaseMapper<GatewayGroupDO> {

    String getIdByKey(String key);

    List<GroupSaveReqVO> pageInfo(Page<GroupSaveReqVO> page, GroupPageReqVO reqVO);

    String getServerNameByGroupKey(String groupKey);
}




