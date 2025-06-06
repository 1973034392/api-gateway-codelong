package top.codelong.findsdk.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import top.codelong.findsdk.annotation.ApiInterface;
import top.codelong.findsdk.annotation.ApiMethod;
import top.codelong.findsdk.config.GatewayServerConfig;
import top.codelong.findsdk.vo.InterfaceRegisterVO;
import top.codelong.findsdk.vo.MethodSaveDomain;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class GatewayRegisterService implements BeanPostProcessor {
    @Resource
    private GatewayServerConfig config;
    @Resource
    private ApplicationConfig applicationConfig;
    @Resource
    private Environment environment;

    private final Logger logger = LoggerFactory.getLogger(GatewayRegisterService.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ApiInterface apiInterface = bean.getClass().getAnnotation(ApiInterface.class);
        if (null == apiInterface) return bean;
        // 接口信息注册
        Class<?>[] interfaces = bean.getClass().getInterfaces();
        if (interfaces.length != 1) {
            throw new RuntimeException("接口类只能有一个接口");
        }
        InterfaceRegisterVO registerVO = new InterfaceRegisterVO();
        Class<?> interfaceClass = interfaces[0];
        registerVO.setInterfaceName(interfaceClass.getName());
        registerVO.setSafeKey(config.getSafeKey());
        registerVO.setSafeSecret(config.getSafeSecret());
        // 设置ip信息
        String serverPort = environment.getProperty("server.port", "8080");
        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }
        registerVO.setServerUrl(localIp + ":" + serverPort);

        List<MethodSaveDomain> methodList = new ArrayList<>();

        // 3. 方法信息
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            ApiMethod apiProducerMethod = method.getAnnotation(ApiMethod.class);
            if (apiProducerMethod == null) continue;

            // 构建方法元数据
            MethodSaveDomain saveDomain = buildMethodMetadata(method, apiProducerMethod);
            methodList.add(saveDomain);
            if (apiProducerMethod.isHttp() == 0) {
                // Dubbo服务暴露
                exposeMethodService(bean, interfaceClass, saveDomain);
            }
        }
        registerVO.setMethods(methodList);
        // 接口信息注册
        register(registerVO);

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    // 构建方法元数据
    private MethodSaveDomain buildMethodMetadata(Method method, ApiMethod apiProducerMethod) {
        MethodSaveDomain saveDomain = new MethodSaveDomain();
        Class<?>[] parameterTypes = method.getParameterTypes();
        StringBuilder parameters = new StringBuilder();
        for (Class<?> clazz : parameterTypes) {
            parameters.append(clazz.getName()).append(",");
        }
        String parameterType = parameters.substring(0, parameters.toString().lastIndexOf(","));

        saveDomain.setMethodName(method.getName());
        saveDomain.setParameterType(parameterType);
        saveDomain.setUrl(apiProducerMethod.url());
        saveDomain.setIsAuth(apiProducerMethod.isAuth());
        saveDomain.setIsHttp(apiProducerMethod.isHttp());
        saveDomain.setHttpType(apiProducerMethod.httpType().getValue());
        return saveDomain;
    }

    private void exposeMethodService(Object bean, Class<?> interfaceClass, MethodSaveDomain saveDomain) {
        // 所有方法共享同一端口，但通过 group 区分
        ProtocolConfig protocolConfig = new ProtocolConfig("dubbo", 20880);
        RegistryConfig registryConfig = new RegistryConfig(RegistryConfig.NO_AVAILABLE);

        ServiceConfig serviceConfig = new ServiceConfig<>();
        serviceConfig.setApplication(applicationConfig);
        serviceConfig.setProtocol(protocolConfig);
        serviceConfig.setRegistry(registryConfig);
        serviceConfig.setInterface(interfaceClass);
        serviceConfig.setRef(bean);

        // 使用方法名作为 group，确保服务唯一性
        serviceConfig.setGroup("method-group-" + saveDomain.getMethodName());

        serviceConfig.export();
        // 输出对应的 URL
        saveDomain.setUrl(serviceConfig.getExportedUrls().get(0).toString());
    }


    public void register(InterfaceRegisterVO registerVO) {
        // 注册完成，执行事件通知
        String addr = config.getCenterAddr();
        String fullUrl = addr + "/gateway-interface/create";

        HttpRequest request = HttpUtil.createRequest(cn.hutool.http.Method.POST, fullUrl);
        request.header("Content-Type", "application/json");

        request.body(JSON.toJSONString(registerVO));
        HttpResponse response;
        try {
            response = request.execute();
        } catch (Exception e) {
            logger.error("接口注册失败: {}", e.getMessage());
            return;
        }
        logger.info("接口注册成功: {}", response.body());
    }
}