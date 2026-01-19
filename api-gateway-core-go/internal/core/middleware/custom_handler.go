package middleware

import (
	"net/http"

	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/pkg/logger"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

// PreHandler 定义前置处理器接口
type PreHandler interface {
	Handle(c *gin.Context, stmt *entity.HttpStatement) error
	Name() string
}

// PostHandler 定义后置处理器接口
type PostHandler interface {
	Handle(c *gin.Context, stmt *entity.HttpStatement, response interface{}) error
	Name() string
}

var (
	preHandlers  []PreHandler
	postHandlers []PostHandler
)

func RegisterPreHandler(h PreHandler) {
	preHandlers = append(preHandlers, h)
}

func RegisterPostHandler(h PostHandler) {
	postHandlers = append(postHandlers, h)
}

func CustomHandlerMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		v, exists := c.Get(ContextKeyHttpStatement)
		if !exists {
			c.Next()
			return
		}
		stmt := v.(*entity.HttpStatement)

		// 执行前置处理器
		for _, h := range preHandlers {
			if err := h.Handle(c, stmt); err != nil {
				logger.Error("PreHandler执行失败", zap.String("handler", h.Name()), zap.Error(err))
				c.AbortWithStatusJSON(http.StatusInternalServerError, gin.H{"error": "PreHandler failed"})
				return
			}
		}

		c.Next()

		// 执行后置处理器
		// 如果 handler 已经写入了响应，我们可能无法修改 Body，但可以做日志或异步处理
		// 我们获取 Response (如果是 ProxyHandler 写入的)
		// 但 ProxyHandler 直接 c.Data 写了，这里很难拿到结构化的响应，除非我们包装 ResponseWriter。
		// 暂时只做简单的通知式调用
		for _, h := range postHandlers {
			// 这里的 response 参数暂时传 nil，因为 Gin 获取响应体需要 hack ResponseWriter
			if err := h.Handle(c, stmt, nil); err != nil {
				logger.Error("PostHandler执行失败", zap.String("handler", h.Name()), zap.Error(err))
			}
		}
	}
}
