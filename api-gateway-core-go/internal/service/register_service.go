package service

import (
	"encoding/json"
	"fmt"
	"strconv"
	"strings"

	"api-gateway-core-go/config"
	"api-gateway-core-go/internal/model/vo"
	"api-gateway-core-go/pkg/logger"
	"api-gateway-core-go/pkg/response"
	"api-gateway-core-go/pkg/utils"

	"github.com/go-resty/resty/v2"
	"go.uber.org/zap"
)

type RegisterService struct {
	client *resty.Client
}

func NewRegisterService() *RegisterService {
	return &RegisterService{
		client: resty.New(),
	}
}

func (s *RegisterService) Register() error {
	logger.Info("开始向网关中心注册服务")

	cfg := config.GlobalConfig.ApiGateway
	url := cfg.GatewayCenter + "/gateway-group-detail/register"

	// 尝试通过连接网关中心来获取本机IP
	// 解析 GatewayCenter URL 提取 host
	var localIP string
	// 简单的 URL 解析，如果 cfg.GatewayCenter 包含 http/https 前缀
	targetAddr := cfg.GatewayCenter
	if idx := strings.Index(targetAddr, "://"); idx != -1 {
		targetAddr = targetAddr[idx+3:]
	}
	// 去掉可能存在的 path
	if idx := strings.Index(targetAddr, "/"); idx != -1 {
		targetAddr = targetAddr[:idx]
	}
	// 确保有端口，如果没有端口，默认80 (Dial UDP 需要端口)
	if !strings.Contains(targetAddr, ":") {
		targetAddr += ":80"
	}

	outboundIP, err := utils.GetOutboundIP(targetAddr)
	if err == nil && outboundIP != "" {
		localIP = outboundIP
	} else {
		// 降级方案
		localIP = utils.GetLocalIP()
		logger.Warn("无法通过连接网关中心获取IP，使用本地遍历结果", zap.Error(err), zap.String("localIP", localIP))
	}

	address := localIP + ":" + strconv.Itoa(cfg.Port)

	reqVO := vo.GroupRegisterReqVO{
		GroupKey:      cfg.GroupKey,
		DetailName:    config.GlobalConfig.ApiGateway.GroupKey + "-core-go", // 暂时命名
		DetailAddress: address,
		DetailWeight:  cfg.Weight,
	}

	logger.Debug("注册请求参数", zap.Any("req", reqVO))

	resp, err := s.client.R().
		SetHeader("Content-Type", "application/json").
		SetBody(reqVO).
		Post(url)

	if err != nil {
		logger.Error("服务注册失败", zap.String("url", url), zap.Error(err))
		return err
	}

	logger.Debug("注册响应", zap.String("body", string(resp.Body())))

	var result response.Result[vo.GroupDetailRegisterRespVO]
	if err := json.Unmarshal(resp.Body(), &result); err != nil {
		logger.Error("解析响应失败", zap.Error(err))
		return err
	}

	// 网关中心返回的 Code 为 1 表示成功
	if result.Code != 1 {
		return fmt.Errorf("服务注册失败: %s", result.Message)
	}

	registerResp := result.Data

	config.GlobalConfig.ServerName = registerResp.ServerName
	config.GlobalConfig.SafeKey = registerResp.SafeKey
	config.GlobalConfig.SafeSecret = registerResp.SafeSecret

	logger.Info("服务注册成功", zap.String("serverName", registerResp.ServerName), zap.String("safeKey", registerResp.SafeKey))
	return nil
}

func (s *RegisterService) Deregister() {
	logger.Info("开始从网关中心下线服务")
	// 我们的心跳是 RedisListener 收到 Redis 消息后触发的，或者定时触发。
	// 优雅停机时，只要进程退出，心跳自然停止。
	// 但如果有资源需要清理，可以在这里做。
	logger.Info("服务下线逻辑执行完成")
}
