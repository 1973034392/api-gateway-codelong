package top.codelong.apigatewaycenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.codelong.apigatewaycenter.common.page.PageResult;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import top.codelong.apigatewaycenter.dto.req.InterfaceMethodSaveReqVO;

import java.util.List;

/**
* @author CodeLong
* @description 针对表【gateway_interface(接口信息表)】的数据库操作Service
* @createDate 2025-05-23 16:05:44
*/
public interface GatewayInterfaceService extends IService<GatewayInterfaceDO> {

    Long create(InterfaceMethodSaveReqVO reqVO);

    /**
     * 分页查询接口列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param serverId 服务ID（可选）
     * @return 分页结果
     */
    PageResult<GatewayInterfaceDO> page(Integer pageNum, Integer pageSize, Long serverId);

    /**
     * 获取所有接口列表
     * @return 接口列表
     */
    List<GatewayInterfaceDO> list();
}
