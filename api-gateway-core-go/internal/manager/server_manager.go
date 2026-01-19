package manager

import (
	"context"
	"fmt"
	"math/rand"
	"strings"
	"sync"
	"time"

	"api-gateway-core-go/config"
	"api-gateway-core-go/pkg/logger"

	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

type ServerManager struct {
	redisClient *redis.Client
	servers     []string
	rwLock      sync.RWMutex
}

var ServerMgr *ServerManager

func InitServerManager() {
	rCfg := config.GlobalConfig.Redis.Data.Redis
	rdb := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", rCfg.Host, rCfg.Port),
		DB:       rCfg.Database,
		Password: "",
	})

	ServerMgr = &ServerManager{
		redisClient: rdb,
		servers:     make([]string, 0),
	}

	// 初始更新
	ServerMgr.Update()

	// 启动定时更新 (这里为了简单加个定时兜底，也可以监听 Redis 消息)
	// 为了保持一致性，后续应补充 Redis 监听。
	go func() {
		ticker := time.NewTicker(30 * time.Second)
		for range ticker.C {
			ServerMgr.Update()
		}
	}()
	
	logger.Info("服务管理器初始化完成")
}

func (m *ServerManager) Update() {
	pattern := fmt.Sprintf("heartbeat:server:%s:*", config.GlobalConfig.ServerName)
	ctx := context.Background()
	
	// 使用 Scan 替代 Keys
	var keys []string
	var cursor uint64
	var err error
	
	for {
		var ks []string
		ks, cursor, err = m.redisClient.Scan(ctx, cursor, pattern, 100).Result()
		if err != nil {
			logger.Error("扫描服务实例失败", zap.Error(err))
			return
		}
		keys = append(keys, ks...)
		if cursor == 0 {
			break
		}
	}

	if len(keys) == 0 {
		logger.Warn("未找到可用的服务器实例", zap.String("pattern", pattern))
		m.rwLock.Lock()
		m.servers = []string{}
		m.rwLock.Unlock()
		return
	}

	newServers := make([]string, 0)
	for _, key := range keys {
		// key format: heartbeat:server:{serverName}:{ip}:{port}
		parts := strings.Split(key, ":")
		if len(parts) >= 5 {
			addr := parts[3] + ":" + parts[4]
			newServers = append(newServers, addr)
		}
	}

	m.rwLock.Lock()
	m.servers = newServers
	m.rwLock.Unlock()

	logger.Debug("服务器列表更新完成", zap.Int("count", len(newServers)), zap.Strings("servers", newServers))
}

func (m *ServerManager) GetOne() (string, error) {
	m.rwLock.RLock()
	defer m.rwLock.RUnlock()

	if len(m.servers) == 0 {
		return "", fmt.Errorf("没有可用的服务器")
	}

	// 随机负载均衡
	idx := rand.Intn(len(m.servers))
	return m.servers[idx], nil
}
