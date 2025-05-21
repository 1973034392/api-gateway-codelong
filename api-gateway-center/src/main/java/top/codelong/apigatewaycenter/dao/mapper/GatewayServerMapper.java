package top.codelong.apigatewaycenter.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;

/**
 * @author codelong
 * @description 针对表【gateway_server(网关系统表)】的数据库操作Mapper
 * @createDate 2025-05-21 18:07:52
 * @Entity top.codelong.apigatewaycenter.dao.entity.GatewayServerDO
 */
@Mapper
public interface GatewayServerMapper extends BaseMapper<GatewayServerDO> {

}




