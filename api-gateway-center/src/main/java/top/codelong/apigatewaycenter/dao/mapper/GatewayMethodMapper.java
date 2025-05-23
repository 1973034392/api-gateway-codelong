package top.codelong.apigatewaycenter.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author Administrator
* @description 针对表【gateway_method(方法信息表)】的数据库操作Mapper
* @createDate 2025-05-23 16:05:44
* @Entity top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO
*/
@Mapper
public interface GatewayMethodMapper extends BaseMapper<GatewayMethodDO> {

}




