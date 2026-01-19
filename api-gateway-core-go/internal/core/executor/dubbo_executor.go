package executor

import (
	"context"
	"fmt"
	"sync"

	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/pkg/logger"

	"dubbo.apache.org/dubbo-go/v3/config"
	"dubbo.apache.org/dubbo-go/v3/config/generic"
	_ "dubbo.apache.org/dubbo-go/v3/imports"
	_ "dubbo.apache.org/dubbo-go/v3/protocol/dubbo"
	hessian "github.com/apache/dubbo-go-hessian2"
	"go.uber.org/zap"
)

type DubboExecutor struct {
	// 缓存 GenericService
	serviceMap sync.Map
}

var DefaultDubboExecutor *DubboExecutor

func InitDubboExecutor() {
	// 在简单直连模式下，我们只需要最基础的 RootConfig
	// 不需要复杂的 Shutdown 配置，因为我们不作为 Server 运行
	if config.GetRootConfig() == nil {
		rc := config.NewRootConfigBuilder().
			SetConsumer(config.NewConsumerConfigBuilder().
				SetCheck(false). // 关闭启动检查
				Build()).
			Build()
		config.SetRootConfig(*rc)
		// 尝试加载一次，即使失败也不影响后续的直连逻辑
		_ = config.Load()
	}
	DefaultDubboExecutor = &DubboExecutor{}
}

func (e *DubboExecutor) Execute(params map[string]interface{}, targetUrl string, stmt *entity.HttpStatement) (interface{}, error) {
	interfaceName := stmt.InterfaceName
	// 缓存 Key：URL + 接口名
	cacheKey := fmt.Sprintf("%s:%s", targetUrl, interfaceName)

	if val, ok := e.serviceMap.Load(cacheKey); ok {
		// 缓存命中，直接转换并返回
		if gs, ok := val.(*generic.GenericService); ok {
			return invokeGeneric(gs, stmt.MethodName, stmt.ParameterType, params)
		}
	}

	// 缓存未命中，构建新的 Reference

	// 1. 构建直连 URL
	dubboUrl := fmt.Sprintf("dubbo://%s/%s", targetUrl, interfaceName)

	// 2. 构建 ReferenceConfig
	refConf := config.NewReferenceConfigBuilder().
		SetInterface(interfaceName).
		SetURL(dubboUrl). // 直连地址
		SetGeneric(true). // 泛化调用
		SetProtocol("dubbo"). // 协议
		SetSerialization("hessian2"). // 序列化
		SetFilter("-cshutdown,-graceful_shutdown,-metrics").
		SetGroup("method-group-test").
		Build()

	// 3. 初始化泛化服务对象
	gs := generic.NewGenericService(interfaceName)

	// 4. 初始化配置 (忽略 RootConfig 的复杂依赖)
	// 这里即使 RootConfig 没完全 ready，因为我们禁用了 filter，也能跑通
	if err := refConf.Init(config.GetRootConfig()); err != nil {
		logger.Error("Dubbo Init failed", zap.Error(err))
		return nil, err
	}

	// 5. 引用服务
	refConf.Refer(gs)
	refConf.Implement(gs)

	// 6. 确保 Invoke 可用
	if gs.Invoke == nil {
		// 尝试从底层 RPCService 恢复
		if service := refConf.GetRPCService(); service != nil {
			if g, ok := service.(*generic.GenericService); ok {
				gs = g
			}
		}
	}

	if gs.Invoke == nil {
		return nil, fmt.Errorf("generic service invoke is nil")
	}

	// 7. 存入缓存
	e.serviceMap.Store(cacheKey, gs)

	// 8. 执行调用
	return invokeGeneric(gs, stmt.MethodName, stmt.ParameterType, params)
}

// invokeGeneric 封装调用逻辑，让主流程更清晰
func invokeGeneric(gs *generic.GenericService, methodName string, paramTypes []string, params map[string]interface{}) (interface{}, error) {
	if paramTypes == nil {
		paramTypes = []string{}
	}

	var args []interface{}
	// 简单的参数映射逻辑
	if len(params) > 0 {
		for _, v := range params {
			args = append(args, v)
		}
	} else {
		args = []interface{}{}
	}

	// 转换为 hessian 对象
	hessianArgs := make([]hessian.Object, len(args))
	for i, v := range args {
		hessianArgs[i] = v
	}

	// 发起 RPC 调用
	resp, err := gs.Invoke(context.Background(), methodName, paramTypes, hessianArgs)
	if err != nil {
		logger.Error("Dubbo Invoke failed", zap.Error(err))
		return nil, err
	}

	return resp, nil
}
