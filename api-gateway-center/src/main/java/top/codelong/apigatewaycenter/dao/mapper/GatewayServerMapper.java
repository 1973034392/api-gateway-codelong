package top.codelong.apigatewaycenter.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dto.req.ServerSaveReqVO;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【gateway_server(网关系统表)】的数据库操作Mapper
 * @createDate 2025-05-23 16:05:44
 * @Entity top.codelong.apigatewaycenter.dao.entity.GatewayServerDO
 */
@Mapper
public interface GatewayServerMapper extends BaseMapper<GatewayServerDO> {

    boolean nameIsExist(String name);

    List<GatewayServerDO> pageInfo(Page<ServerSaveReqVO> page, String name, Integer status, String addr);

    Long getIdBySafeKey(String safeKey);

    String getServerNameBySafeKey(String safeKey);

    GatewayServerDO getServerByGroupId(Long groupId);
}




