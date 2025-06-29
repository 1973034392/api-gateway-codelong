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

/**
 * 网关注册服务
 * 负责自动注册API接口到网关中心
 */
public class GatewayRegisterService implements BeanPostProcessor {
    @Resource
    private GatewayServerConfig config;
    @Resource
    private ApplicationConfig applicationConfig;
    @Resource
    private Environment environment;

    private final Logger logger = LoggerFactory.getLogger(GatewayRegisterService.class);

    /**
     * Bean初始化后处理
     * 扫描带有@ApiInterface注解的Bean并注册到网关中心
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        logger.debug("开始处理Bean: {}", beanName);

        // 检查是否带有ApiInterface注解
        ApiInterface apiInterface = bean.getClass().getAnnotation(ApiInterface.class);
        if (null == apiInterface) {
            logger.trace("Bean {} 未包含ApiInterface注解，跳过处理", beanName);
            return bean;
        }

        logger.info("发现API接口类: {}", bean.getClass().getName());

        // 验证接口实现
        Class<?>[] interfaces = bean.getClass().getInterfaces();
        if (interfaces.length != 1) {
            logger.error("接口类必须实现且仅实现一个接口");
            throw new RuntimeException("接口类只能有一个接口");
        }

        // 构建接口注册信息
        InterfaceRegisterVO registerVO = new InterfaceRegisterVO();
        Class<?> interfaceClass = interfaces[0];
        registerVO.setInterfaceName(interfaceClass.getName());
        registerVO.setSafeKey(config.getSafeKey());
        registerVO.setSafeSecret(config.getSafeSecret());

        // 设置服务地址
        String serverPort = environment.getProperty("server.port", "8080");
        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
            logger.debug("获取本地IP地址: {}", localIp);
        } catch (Exception e) {
            logger.warn("获取本地IP地址失败，使用默认localhost", e);
        }
        registerVO.setServerUrl(localIp + ":" + serverPort);
        logger.info("服务地址: {}", registerVO.getServerUrl());

        // 处理方法信息
        List<MethodSaveDomain> methodList = new ArrayList<>();
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            ApiMethod apiProducerMethod = method.getAnnotation(ApiMethod.class);
            if (apiProducerMethod == null) {
                logger.trace("方法 {} 未包含ApiMethod注解，跳过处理", method.getName());
                continue;
            }

            logger.debug("处理方法: {}", method.getName());
            MethodSaveDomain saveDomain = buildMethodMetadata(method, apiProducerMethod);
            methodList.add(saveDomain);

            // 非HTTP方法需要暴露Dubbo服务
            if (apiProducerMethod.isHttp() == 0) {
                logger.info("暴露Dubbo服务: {}", method.getName());
                exposeMethodService(bean, interfaceClass, saveDomain);
            }
        }
        registerVO.setMethods(methodList);

        // 注册接口到网关中心
        logger.info("开始注册接口到网关中心...");
        register(registerVO);

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    /**
     * 构建方法元数据
     * @param method 方法对象
     * @param apiProducerMethod 方法注解
     * @return 方法元数据对象
     */
    private MethodSaveDomain buildMethodMetadata(Method method, ApiMethod apiProducerMethod) {
        logger.debug("构建方法元数据: {}", method.getName());

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

        logger.trace("方法 {} 元数据: {}", method.getName(), saveDomain);
        return saveDomain;
    }

    /**
     * 暴露Dubbo服务
     * @param bean 服务实现类
     * @param interfaceClass 接口类
     * @param saveDomain 方法元数据
     */
    private void exposeMethodService(Object bean, Class<?> interfaceClass, MethodSaveDomain saveDomain) {
        logger.debug("开始暴露Dubbo服务: {}", saveDomain.getMethodName());

        // Dubbo协议配置
        ProtocolConfig protocolConfig = new ProtocolConfig("dubbo", 20880);
        RegistryConfig registryConfig = new RegistryConfig(RegistryConfig.NO_AVAILABLE);

        // 服务配置
        ServiceConfig serviceConfig = new ServiceConfig<>();
        serviceConfig.setApplication(applicationConfig);
        serviceConfig.setGeneric(String.valueOf(true));
        serviceConfig.setProtocol(protocolConfig);
        serviceConfig.setRegistry(registryConfig);
        serviceConfig.setInterface(interfaceClass);
        serviceConfig.setRef(bean);

        // 使用方法名作为分组
        serviceConfig.setGroup("method-group-" + saveDomain.getMethodName());

        // 暴露服务
        serviceConfig.export();
        logger.info("Dubbo服务 {} 暴露成功", saveDomain.getMethodName());
    }

    /**
     * 注册接口到网关中心
     * @param registerVO 接口注册信息
     */
    public void register(InterfaceRegisterVO registerVO) {
        logger.info("开始注册接口到网关中心...");

        String addr = config.getCenterAddr();
        String fullUrl = addr + "/gateway-interface/create";
        logger.debug("网关中心地址: {}", fullUrl);

        // 构建HTTP请求
        HttpRequest request = HttpUtil.createRequest(cn.hutool.http.Method.POST, fullUrl);
        request.header("Content-Type", "application/json");
        request.body(JSON.toJSONString(registerVO));

        logger.trace("注册请求体: {}", JSON.toJSONString(registerVO));

        // 发送请求
        HttpResponse response;
        try {
            response = request.execute();
            logger.info("接口注册成功: {}", response.body());
        } catch (Exception e) {
            logger.error("接口注册失败", e);
        }
    }
}