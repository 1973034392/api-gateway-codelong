package top.codelong.apigatewaycenter.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author CodeLong
* @description 针对表【gateway_interface(接口信息表)】的数据库操作Mapper
* @createDate 2025-05-23 16:05:44
* @Entity top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO
*/
@Mapper
public interface GatewayInterfaceMapper extends BaseMapper<GatewayInterfaceDO> {

}




