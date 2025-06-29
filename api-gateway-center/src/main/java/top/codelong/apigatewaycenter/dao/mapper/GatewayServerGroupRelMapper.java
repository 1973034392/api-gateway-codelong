package top.codelong.apigatewaycenter.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author CodeLong
* @description 针对表【gateway_server_group_rel(网关系统和实例分组关联表)】的数据库操作Mapper
* @createDate 2025-05-23 16:05:44
* @Entity top.codelong.apigatewaycenter.dao.entity.GatewayServerGroupRelDO
*/
@Mapper
public interface GatewayServerGroupRelMapper extends BaseMapper<GatewayServerGroupRelDO> {

}




