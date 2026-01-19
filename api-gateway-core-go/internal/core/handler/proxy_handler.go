package handler

import (
	"encoding/json"
	"net/http"

	"strings"

	"api-gateway-core-go/config"
	"api-gateway-core-go/internal/core/executor"
	"api-gateway-core-go/internal/core/middleware"
	"api-gateway-core-go/internal/manager"
	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/pkg/logger"
	"api-gateway-core-go/pkg/response"
	"api-gateway-core-go/pkg/utils"

	"github.com/gin-gonic/gin"
	lru "github.com/hashicorp/golang-lru/v2"
	"go.uber.org/zap"
)

var responseCache *lru.Cache[string, []byte]

func InitProxyHandler() {
	var err error
	maxCache := config.GlobalConfig.ApiGateway.MaxCache
	if maxCache <= 0 {
		maxCache = 1000
	}
	responseCache, err = lru.New[string, []byte](maxCache)
	if err != nil {
		panic(err)
	}
}

func ProxyHandler(c *gin.Context) {
	v, exists := c.Get(middleware.ContextKeyHttpStatement)
	if !exists {
		c.JSON(http.StatusInternalServerError, response.Result[any]{Code: 500, Message: "HttpStatement missing"})
		return
	}
	stmt := v.(*entity.HttpStatement)

	// 1. 检查缓存
	cacheKey := c.Request.Method + ":" + c.Request.RequestURI
	if val, ok := responseCache.Get(cacheKey); ok {
		c.Data(http.StatusOK, "application/json; charset=utf-8", val)
		return
	}

	// 2. 解析参数
	params, err := utils.ParseParameters(c)
	if err != nil {
		logger.Error("参数解析失败", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Result[any]{Code: 400, Message: "参数解析失败"})
		return
	}

	// 3. 获取服务地址
	serverAddr, err := manager.ServerMgr.GetOne()
	if err != nil {
		logger.Error("获取服务地址失败", zap.Error(err))
		c.JSON(http.StatusServiceUnavailable, response.Result[any]{Code: 503, Message: "服务不可用"})
		return
	}

	// 4. 执行请求
	var resultStr string
	var resultObj interface{}

	if stmt.IsHttp {
		targetUrl := "http://" + serverAddr + c.Request.URL.Path
		resultStr, err = executor.DefaultHttpExecutor.Execute(params, targetUrl, stmt)
		resultObj = resultStr
	} else {
		// Dubbo 调用
		// Dubbo 端口默认 20880
		// serverAddr 是 ip:port (HTTP port), 需要提取 IP
		host := serverAddr
		if idx := strings.Index(serverAddr, ":"); idx > 0 {
			host = serverAddr[:idx]
		}
		dubboAddr := host + ":20880"

		var resp interface{}
		resp, err = executor.DefaultDubboExecutor.Execute(params, dubboAddr, stmt)
		if err == nil {
			// Dubbo 结果是 interface{}，可能是任何类型
			resultObj = resp
		}
	}

	if err != nil {
		logger.Error("服务调用失败", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Result[any]{Code: 500, Message: "服务调用失败: " + err.Error()})
		return
	}

	// 5. 包装结果并缓存
	// 如果是 HTTP，resultObj 是 string，尝试解析 JSON
	if str, ok := resultObj.(string); ok {
		var jsonObj interface{}
		if json.Unmarshal([]byte(str), &jsonObj) == nil {
			resultObj = jsonObj
		}
	}

	res := response.Result[interface{}]{
		Code:    200,
		Message: "success",
		Data:    resultObj,
	}

	respBytes, _ := json.Marshal(res)
	responseCache.Add(cacheKey, respBytes)

	c.Data(http.StatusOK, "application/json; charset=utf-8", respBytes)
}
