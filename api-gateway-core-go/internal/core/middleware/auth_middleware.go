package middleware

import (
	"net/http"
	"strings"

	"api-gateway-core-go/internal/manager"
	"api-gateway-core-go/pkg/logger"
	"api-gateway-core-go/pkg/response"
	"api-gateway-core-go/pkg/utils"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

const (
	ContextKeyHttpStatement = "HttpStatement"
)

func AuthMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		uri := c.Request.URL.Path
		// 去除 query string (Gin 的 Path 已经去除了)
		
		// 1. 获取接口配置
		stmt, err := manager.RouteMgr.GetStatement(uri)
		if err != nil {
			logger.Error("获取接口配置失败", zap.String("uri", uri), zap.Error(err))
			c.AbortWithStatusJSON(http.StatusInternalServerError, response.Result[any]{
				Code:    500,
				Message: "系统内部错误",
			})
			return
		}

		if stmt == nil {
			logger.Warn("接口不存在", zap.String("uri", uri))
			c.AbortWithStatusJSON(http.StatusNotFound, response.Result[any]{
				Code:    404,
				Message: "接口不存在",
			})
			return
		}

		// 存入 Context 供后续使用
		c.Set(ContextKeyHttpStatement, stmt)

		// 2. 鉴权
		if stmt.IsAuth {
			token := c.GetHeader("Authorization")
			if token == "" {
				logger.Warn("请求缺少Token", zap.String("uri", uri))
				c.AbortWithStatusJSON(http.StatusUnauthorized, response.Result[any]{
					Code:    401,
					Message: "未授权: 缺少Token",
				})
				return
			}
			
			// 某些客户端可能会带 "Bearer " 前缀
			if strings.HasPrefix(token, "Bearer ") {
				token = token[7:]
			}

			if ok, err := utils.VerifyToken(token); !ok {
				logger.Warn("Token验证失败", zap.String("uri", uri), zap.Error(err))
				c.AbortWithStatusJSON(http.StatusForbidden, response.Result[any]{
					Code:    403,
					Message: "无权访问",
				})
				return
			}
		}

		c.Next()
	}
}
