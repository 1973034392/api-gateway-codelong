package top.codelong.apigatewaycenter.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author codelong
* @description 针对表【gateway_group(网关实例分组表)】的数据库操作Mapper
* @createDate 2025-05-21 18:07:52
* @Entity top.codelong.apigatewaycenter.dao.entity.GatewayGroupDO
*/
@Mapper
public interface GatewayGroupMapper extends BaseMapper<GatewayGroupDO> {

}




