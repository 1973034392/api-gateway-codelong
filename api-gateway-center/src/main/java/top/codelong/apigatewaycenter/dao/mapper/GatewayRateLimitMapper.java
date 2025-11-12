package top.codelong.apigatewaycenter.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayRateLimitDO;

/**
 * 网关限流配置Mapper
 */
@Mapper
public interface GatewayRateLimitMapper extends BaseMapper<GatewayRateLimitDO> {
}

