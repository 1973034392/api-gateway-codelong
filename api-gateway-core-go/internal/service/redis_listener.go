package service

import (
	"context"
	"encoding/json"
	"fmt"
	"strconv"

	"api-gateway-core-go/config"
	"api-gateway-core-go/internal/core/ratelimiter"
	"api-gateway-core-go/internal/manager"
	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/pkg/logger"
	"api-gateway-core-go/pkg/utils"

	"github.com/go-resty/resty/v2"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

type HeartbeatService struct {
	client *resty.Client
}

func NewHeartbeatService() *HeartbeatService {
	return &HeartbeatService{
		client: resty.New(),
	}
}

func (s *HeartbeatService) SendHeartbeat() {
	cfg := config.GlobalConfig.ApiGateway
	url := cfg.GatewayCenter + "/gateway-group-detail/keep-alive"

	localIP := utils.GetLocalIP()
	address := localIP + ":" + strconv.Itoa(cfg.Port)

	// 使用 map 或者 struct，这里用 map 保持灵活
	param := map[string]interface{}{
		"groupKey": cfg.GroupKey,
		"addr":     address,
		"weight":   cfg.Weight,
	}

	_, err := s.client.R().
		SetHeader("Content-Type", "application/json").
		SetBody(param).
		Put(url)

	if err != nil {
		logger.Error("心跳发送失败", zap.String("url", url), zap.Error(err))
	} else {
		logger.Debug("心跳发送成功")
	}
}

// RedisListener 负责监听 Redis 消息
type RedisListener struct {
	rdb              *redis.Client
	heartbeatService *HeartbeatService
}

func InitRedisListener() {
	rCfg := config.GlobalConfig.Redis.Data.Redis
	rdb := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", rCfg.Host, rCfg.Port),
		DB:       rCfg.Database,
		Password: "",
	})

	listener := &RedisListener{
		rdb:              rdb,
		heartbeatService: NewHeartbeatService(),
	}

	go listener.Start()
}

func (l *RedisListener) Start() {
	ctx := context.Background()
	// 订阅频道
	pubsub := l.rdb.Subscribe(ctx, "heartBeat", "service-launched", "rate-limit-config-update", "api-gateway-interface-update")
	defer pubsub.Close()

	ch := pubsub.Channel()

	logger.Info("开始监听 Redis 消息 (heartBeat, service-launched, rate-limit-config-update, api-gateway-interface-update)")

	for msg := range ch {
		logger.Debug("收到 Redis 消息", zap.String("channel", msg.Channel))
		switch msg.Channel {
		case "heartBeat":
			// 收到心跳指令，发送心跳
			go l.heartbeatService.SendHeartbeat()
		case "service-launched":
			// 服务变动，更新服务列表
			go manager.ServerMgr.Update()
		case "rate-limit-config-update":
			// 限流配置变更
			l.handleRateLimitConfigUpdate(msg.Payload)
		case "api-gateway-interface-update":
			// 接口信息变更
			url := msg.Payload
			if len(url) >= 2 && url[0] == '"' && url[len(url)-1] == '"' {
				url = url[1 : len(url)-1]
			}
			logger.Info("收到接口更新通知", zap.String("url", url))
			manager.RouteMgr.RemoveCache(url)
		}
	}
}

func (l *RedisListener) handleRateLimitConfigUpdate(payload string) {
	logger.Info("收到限流配置更新", zap.String("payload", payload))
	if payload == "RELOAD_ALL" {
		ratelimiter.Limiter.ClearCache()
		return
	}

	var config entity.RateLimitConfig
	if err := json.Unmarshal([]byte(payload), &config); err != nil {
		logger.Error("解析限流配置失败", zap.Error(err))
		return
	}

	key := buildConfigKey(&config)
	ratelimiter.Limiter.UpdateConfig(key, &config)
	logger.Info("限流配置已更新", zap.String("key", key))
}

func buildConfigKey(config *entity.RateLimitConfig) string {
	switch config.LimitType {
	case "GLOBAL":
		return "GLOBAL"
	case "SERVICE":
		return "SERVICE:" + config.LimitTarget
	case "INTERFACE":
		return "INTERFACE:" + config.LimitTarget
	case "IP":
		return "IP:" + config.LimitTarget
	default:
		return config.LimitType + ":" + config.LimitTarget
	}
}
