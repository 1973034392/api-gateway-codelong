package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"api-gateway-core-go/config"
	"api-gateway-core-go/internal/core/executor"
	"api-gateway-core-go/internal/core/handler"
	"api-gateway-core-go/internal/core/handler/custom/pre"
	"api-gateway-core-go/internal/core/middleware"
	"api-gateway-core-go/internal/core/ratelimiter"
	"api-gateway-core-go/internal/manager"
	"api-gateway-core-go/internal/service"
	"api-gateway-core-go/pkg/logger"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

func main() {
	// 1. 初始化配置
	config.InitConfig()

	// 2. 初始化日志
	logger.InitLogger("debug")
	logger.Info("正在启动网关核心服务...")

	// 3. 初始化各组件
		manager.InitRouteManager()
	manager.InitServerManager()
	executor.InitHttpExecutor()
	executor.InitDubboExecutor()
	ratelimiter.InitRateLimiter(manager.RouteMgr.GetRedisClient()) // 复用 Redis Client
	handler.InitProxyHandler()

	// 注册自定义处理器
	middleware.RegisterPreHandler(&pre.TestPreHandler{})

	// 4. 服务注册 & Redis 监听
	service.InitRedisListener()

	registerService := service.NewRegisterService()
	if err := registerService.Register(); err != nil {
		logger.Error("服务注册失败，程序退出", zap.Error(err))
		// 为了开发方便，注册失败可能不一定要退出，但在生产环境应该退出
		// 这里 panic
		panic(err)
	}

	// 注册成功后，ServerName 已更新，立即拉取一次下游服务列表
	manager.ServerMgr.Update()

	// 5. 启动 Gin Server
	gin.SetMode(gin.ReleaseMode)
	r := gin.New()

	// Middlewares
	r.Use(gin.Recovery())
	r.Use(func(c *gin.Context) {
		logger.Info("请求接入", zap.String("method", c.Request.Method), zap.String("uri", c.Request.RequestURI))
		c.Next()
	})
	r.Use(middleware.AuthMiddleware())
	r.Use(middleware.RateLimitMiddleware())
	r.Use(middleware.CustomHandlerMiddleware())

	// Proxy Handler (Catch all)
	r.NoRoute(handler.ProxyHandler)

	addr := fmt.Sprintf(":%d", config.GlobalConfig.ApiGateway.Port)
	logger.Info("网关服务启动成功", zap.String("addr", addr))

	srv := &http.Server{
		Addr:    addr,
		Handler: r,
	}

	// 启动 HTTP Server (在 Goroutine 中)
	go func() {
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logger.Error("服务启动失败", zap.Error(err))
		}
	}()

	// 优雅停机
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	logger.Info("正在关闭服务...")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		logger.Error("服务关闭异常", zap.Error(err))
	}

	// 反注册/清理资源
	registerService.Deregister()

	logger.Info("服务已停止")
}
