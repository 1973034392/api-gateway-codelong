package pre

import (
	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/pkg/logger"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type TestPreHandler struct{}

func (h *TestPreHandler) Handle(c *gin.Context, stmt *entity.HttpStatement) error {
	logger.Info("执行测试前置处理器", zap.String("uri", c.Request.RequestURI))
	// 可以在这里修改 Request，或者进行额外的校验
	return nil
}

func (h *TestPreHandler) Name() string {
	return "TestPreHandler"
}
