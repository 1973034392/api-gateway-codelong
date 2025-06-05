package top.codelong.apigatewaycenter.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.dao.entity.GatewayInterfaceDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayMethodDO;
import top.codelong.apigatewaycenter.dao.entity.GatewayServerDO;
import top.codelong.apigatewaycenter.dao.mapper.GatewayInterfaceMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayMethodMapper;
import top.codelong.apigatewaycenter.dao.mapper.GatewayServerMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class InterfaceFlushScheduled {
    @Resource
    private GatewayServerMapper gatewayServerMapper;
    @Resource
    private GatewayInterfaceMapper gatewayInterfaceMapper;
    @Resource
    private GatewayMethodMapper gatewayMethodMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时刷新接口信息
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void flushURL() {
        List<GatewayServerDO> serverList = gatewayServerMapper.selectList(new LambdaQueryWrapper<GatewayServerDO>()
                .eq(GatewayServerDO::getStatus, 1));
        for (GatewayServerDO serverDO : serverList) {
            List<GatewayInterfaceDO> interfaceList = gatewayInterfaceMapper.selectList(new LambdaQueryWrapper<GatewayInterfaceDO>()
                    .eq(GatewayInterfaceDO::getServerId, serverDO.getId()));
            boolean clear = true;
            for (GatewayInterfaceDO interfaceDO : interfaceList) {
                List<GatewayMethodDO> methodList = gatewayMethodMapper.selectList(new LambdaQueryWrapper<GatewayMethodDO>()
                        .eq(GatewayMethodDO::getInterfaceId, interfaceDO.getId()));
                for (GatewayMethodDO methodDO : methodList) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("interfaceName", interfaceDO.getInterfaceName());
                    params.put("methodName", methodDO.getMethodName());
                    params.put("parameterType", methodDO.getParameterType());
                    params.put("isAuth", methodDO.getIsAuth());
                    params.put("isHttp", methodDO.getIsHttp());
                    params.put("httpType", methodDO.getHttpType());
                    if (clear) {
                        String deletePrefix = "URL:" + serverDO.getServerName() + ":" + methodDO.getUrl().split("&")[0];
                        Set<String> keys = redisTemplate.keys(deletePrefix + "*");
                        if (!keys.isEmpty()) {
                            redisTemplate.delete(keys);
                        }
                        clear = false;
                    }
                    redisTemplate.opsForHash().putAll("URL:" + serverDO.getServerName() + ":" + methodDO.getUrl(), params);
                    redisTemplate.expire("URL:" + serverDO.getServerName() + ":" + methodDO.getUrl(), 35, TimeUnit.MINUTES);
                }
            }
        }
    }

    /**
     * 定时删除失效的注册中心信息
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void flushServer() {
        //TODO 定时删除失效的注册中心信息
    }
}
