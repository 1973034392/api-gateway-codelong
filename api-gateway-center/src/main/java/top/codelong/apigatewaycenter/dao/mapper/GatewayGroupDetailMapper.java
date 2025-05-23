package top.codelong.apigatewaycenter.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDetailDO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailPageReqVO;
import top.codelong.apigatewaycenter.dto.req.GroupDetailSaveReqVO;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【gateway_group_detail(网关实例信息表)】的数据库操作Mapper
 * @createDate 2025-05-23 16:05:44
 * @Entity top.codelong.apigatewaycenter.dao.entity.GatewayGroupDetailDO
 */
@Mapper
public interface GatewayGroupDetailMapper extends BaseMapper<GatewayGroupDetailDO> {

    List<GroupDetailSaveReqVO> pageInfo(Page<GroupDetailSaveReqVO> page, GroupDetailPageReqVO reqVO);

}




