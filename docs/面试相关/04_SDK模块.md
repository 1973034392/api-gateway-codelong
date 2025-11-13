# 04 SDK模块

## 22. 服务注册SDK的核心功能

### 服务注册SDK的主要功能是什么？
1) **自动扫描注解**: 启动时自动扫描`@ApiInterface`和`@ApiMethod`注解；

2) **接口信息上报**: 将接口元数据上报到网关中心；

3) **Dubbo服务暴露**: 为非HTTP方法暴露Dubbo服务；

4) **心跳维持**: 定期向网关中心发送心跳，保持服务在线；

5) **配置管理**: 管理网关中心地址、安全凭证等配置。

### SDK如何自动扫描@ApiInterface和@ApiMethod注解？
1) 实现`BeanPostProcessor`接口，在Bean初始化后处理；

2) 重写`postProcessAfterInitialization()`方法，检查Bean是否有`@ApiInterface`注解；

3) 如果有注解，遍历Bean的所有方法，查找`@ApiMethod`注解；

4) 提取注解中的元数据(URL、HTTP方法、是否认证等)；

5) 构建方法元数据对象并上报到网关中心。

### SDK如何暴露Dubbo服务？
1) 检查`@ApiMethod`注解的`isHttp`属性：0表示Dubbo服务，1表示HTTP服务；

2) 对于Dubbo服务(isHttp==0)，创建`ServiceConfig`对象；

3) 配置Dubbo协议(默认20880端口)和注册中心(NO_AVAILABLE)；

4) 使用方法名作为分组标识：`method-group-{methodName}`；

5) 调用`serviceConfig.export()`暴露服务。

### SDK如何向网关中心上报服务信息？
1) 构建`InterfaceRegisterVO`对象，包含接口名、服务地址、安全凭证、方法列表；

2) 获取本地IP地址和服务端口，组成服务地址(IP:Port)；

3) 使用HTTP POST请求调用网关中心的`/gateway-interface/create`接口；

4) 请求体为JSON格式的`InterfaceRegisterVO`对象；

5) 网关中心返回成功响应后，接口信息存储到数据库和Redis。

## 23. @ApiInterface和@ApiMethod注解

### 这两个注解的作用分别是什么？
**@ApiInterface**:

1) 标记在接口实现类上；

2) 表示该类是一个网关接口；

3) 触发SDK的自动扫描和注册流程。


**@ApiMethod**:

1) 标记在接口方法上；

2) 表示该方法是一个网关方法；

3) 提供方法的元数据(URL、HTTP方法、是否认证等)。

### 注解中的各个属性分别控制什么？
**@ApiInterface属性**:

1) `interfaceName`: 接口名称(可选，默认为空)。

**@ApiMethod属性**:

1) `isHttp`: 是否为HTTP方法(0=Dubbo, 1=HTTP)；

2) `httpType`: HTTP请求方法(GET/POST/PUT/DELETE)；

3) `url`: 接口URL路径；

4) `isAuth`: 是否需要认证(0=不需要, 1=需要)。

### 如何使用这两个注解？
```java
@ApiInterface
public class UserServiceImpl implements UserService {
    @ApiMethod(isHttp = 1, httpType = HttpTypeEnum.GET, url = "/api/user/list", isAuth = 1)
    public List<User> listUsers() {
        return userList;
    }

    @ApiMethod(isHttp = 0, url = "/api/user/get", isAuth = 0)
    public User getUser(String id) {
        return userMap.get(id);
    }
}
```

### 注解的处理流程是什么？
1) 应用启动时，Spring容器初始化所有Bean；

2) `GatewayRegisterService`的`postProcessAfterInitialization()`被调用；

3) 检查Bean是否有`@ApiInterface`注解；

4) 如果有，遍历所有方法查找`@ApiMethod`注解；

5) 提取注解元数据，构建`MethodSaveDomain`对象；

6) 对于Dubbo方法，调用`exposeMethodService()`暴露服务；

7) 最后调用`register()`向网关中心上报所有接口信息。

## 24. SDK的自动配置

### SDK如何实现自动配置？
1) 使用Spring Boot的自动配置机制；

2) 创建`GatewaySDKAutoConfig`配置类，标注`@Configuration`；

3) 使用`@EnableConfigurationProperties`启用配置属性绑定；

4) 使用`@ConditionalOnProperty`条件注解，当`api-gateway-sdk.enabled=true`时启用(默认启用)；

5) 在配置类中定义Bean，如`GatewayRegisterService`。

### spring.factories文件的作用是什么？
1) **自动配置入口**: 告诉Spring Boot在启动时自动加载哪些配置类；

2) **文件位置**: `META-INF/spring.factories`；

3) **内容**: `org.springframework.boot.autoconfigure.EnableAutoConfiguration=top.codelong.findsdk.config.GatewaySDKAutoConfig`；

4) **作用**: 无需在应用中显式导入配置类，Spring Boot会自动发现并加载；

5) **优势**: 实现SDK的零配置集成。

### GatewaySDKAutoConfig类的职责是什么？
1) **配置加载**: 使用`@EnableConfigurationProperties`加载`GatewayServerConfig`配置；

2) **条件判断**: 使用`@ConditionalOnProperty`判断是否启用SDK；

3) **Bean创建**: 创建`GatewayRegisterService` Bean，用于自动注册接口；

4) **依赖注入**: 将配置和服务注入到Spring容器；

5) **生命周期管理**: 管理SDK组件的初始化和销毁。

### 如何禁用SDK的自动配置？
1) **配置文件禁用**: 在`application.yml`中添加`api-gateway-sdk.enabled=false`；

2) **代码禁用**: 使用`@SpringBootApplication(exclude={GatewaySDKAutoConfig.class})`排除配置类；

3) **条件禁用**: 当`api-gateway-sdk.enabled`属性不存在或为false时自动禁用；

4) **效果**: 禁用后SDK不会自动扫描和注册接口。

## 25. SDK的心跳维持

### SDK如何维持心跳？
1) 创建`HeartbeatService`组件，定期发送心跳请求；

2) 监听Redis的`heartBeat`频道，收到消息时触发心跳；

3) 心跳请求包含：`safeKey`(安全凭证)和`addr`(服务地址)；

4) 使用HTTP PUT请求调用网关中心的`/gateway-server-detail/keep-alive`接口；

5) 网关中心收到心跳后更新Redis中的心跳记录，设置30秒过期时间。

### 心跳消息如何通过Redis传递？
1) **发送端**: 网关中心定时任务每15秒发布消息到Redis的`heartBeat`频道；

2) **接收端**: SDK通过`RedisMessageListenerContainer`监听`heartBeat`频道；

3) **监听器**: `RedisMessageListener`实现`MessageListener`接口，接收消息；

4) **处理**: 收到消息后调用`HeartbeatService.heartbeat()`发送心跳；

5) **优势**: 解耦网关中心和SDK，支持多个SDK同时接收消息。

### 如何处理心跳失败？
1) **异常捕获**: 在`HeartbeatService.heartbeat()`中使用try-catch捕获异常；

2) **日志记录**: 记录ERROR级别日志，包含异常信息；

3) **不中断**: 心跳失败不影响服务运行，继续等待下一个心跳周期；

4) **重试**: 下一个心跳周期会重新尝试发送；

5) **监控告警**: 可集成监控系统，心跳失败次数过多时触发告警。

### 心跳维持的频率是多少？
1) **心跳间隔**: 15秒(由网关中心的定时任务控制)；

2) **过期时间**: 30秒(Redis Key的TTL)；

3) **设置原因**: 过期时间是心跳间隔的2倍，允许一次心跳丢失；

4) **自动下线**: 如果30秒内未收到心跳，Redis自动删除记录，服务自动下线；

5) **及时性**: 30秒内能及时发现故障节点，不会太长导致故障转移慢。

