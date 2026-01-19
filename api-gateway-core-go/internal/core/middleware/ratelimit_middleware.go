package middleware

import (
	"net/http"

	"api-gateway-core-go/internal/core/ratelimiter"
	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/pkg/response"

	"github.com/gin-gonic/gin"
)

func RateLimitMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		// 获取 HttpStatement
		v, exists := c.Get(ContextKeyHttpStatement)
		if !exists {
			c.Next()
			return
		}
		stmt := v.(*entity.HttpStatement)

		// 1. 全局限流
		if !checkLimit("GLOBAL") {
			abortWith429(c, "系统繁忙，请稍后重试")
			return
		}

		// 2. 服务限流
		if !checkLimit("SERVICE:"+stmt.GetServiceId()) {
			abortWith429(c, "服务访问频繁，请稍后重试")
			return
		}

		// 3. 接口限流
		url := c.Request.URL.Path
		// Key: INTERFACE:serviceId:url
		if !checkLimit("INTERFACE:" + stmt.GetServiceId() + ":" + url) {
			abortWith429(c, "接口访问频繁，请稍后重试")
			return
		}

		// 4. IP 限流
		ip := c.ClientIP()
		if !checkLimit("IP:"+ip) {
			abortWith429(c, "访问过于频繁，请稍后重试")
			return
		}

		c.Next()
	}
}

func checkLimit(key string) bool {
	config := ratelimiter.Limiter.GetConfig(key)
	// 如果没有配置，默认不限流
	if config == nil {
		return true
	}
	return ratelimiter.Limiter.TryAcquire(key, config)
}

func abortWith429(c *gin.Context, msg string) {
	c.AbortWithStatusJSON(http.StatusTooManyRequests, response.Result[any]{
		Code:    429,
		Message: msg,
	})
}
