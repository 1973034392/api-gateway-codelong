package manager

import (
	"context"
	"fmt"
	"strings"

	"api-gateway-core-go/config"
	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/internal/model/enum"
	"api-gateway-core-go/pkg/logger"

	lru "github.com/hashicorp/golang-lru/v2"
	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

type RouteManager struct {
	redisClient *redis.Client
	localCache  *lru.Cache[string, *entity.HttpStatement]
}

var RouteMgr *RouteManager

func InitRouteManager() {
	// 初始化 Redis
	rCfg := config.GlobalConfig.Redis.Data.Redis
	rdb := redis.NewClient(&redis.Options{
		Addr:     fmt.Sprintf("%s:%d", rCfg.Host, rCfg.Port),
		DB:       rCfg.Database,
		Password: "",
	})

	// 测试连接
	if err := rdb.Ping(context.Background()).Err(); err != nil {
		logger.Error("无法连接到 Redis", zap.Error(err))
		panic(err)
	}

	// 初始化 LRU Cache
	maxCache := config.GlobalConfig.ApiGateway.MaxCache
	if maxCache <= 0 {
		maxCache = 1000
	}
	cache, err := lru.New[string, *entity.HttpStatement](maxCache)
	if err != nil {
		logger.Error("无法初始化本地缓存", zap.Error(err))
		panic(err)
	}

	RouteMgr = &RouteManager{
		redisClient: rdb,
		localCache:  cache,
	}

	logger.Info("路由管理器初始化完成", zap.Int("maxCache", maxCache))
}

func (m *RouteManager) GetStatement(url string) (*entity.HttpStatement, error) {
	// 1. 查本地缓存
	if val, ok := m.localCache.Get(url); ok {
		return val, nil
	}

	// 2. 查 Redis
	stmt, err := m.getStatementFromRedis(url)
	if err != nil {
		return nil, err
	}
	if stmt == nil {
		return nil, nil
	}

	// 3. 写入本地缓存
	m.localCache.Add(url, stmt)
	return stmt, nil
}

func (m *RouteManager) RemoveCache(url string) {
	if url == "RELOAD_ALL" {
		m.localCache.Purge()
		logger.Info("已清空所有本地接口配置缓存")
	} else {
		m.localCache.Remove(url)
		logger.Info("已移除本地接口配置缓存", zap.String("url", url))
	}
}

func (m *RouteManager) GetRedisClient() *redis.Client {
	return m.redisClient
}

func (m *RouteManager) getStatementFromRedis(url string) (*entity.HttpStatement, error) {
	key := fmt.Sprintf("URL:%s:%s", config.GlobalConfig.ServerName, url)
	data, err := m.redisClient.HGetAll(context.Background(), key).Result()
	if err != nil {
		return nil, err
	}
	if len(data) == 0 {
		return nil, nil
	}

	isAuth := data["isAuth"] == "1"
	isHttp := data["isHttp"] == "1"

	// 辅助函数：去除可能存在的首尾双引号
	cleanStr := func(s string) string {
		if len(s) >= 2 && s[0] == '"' && s[len(s)-1] == '"' {
			return s[1 : len(s)-1]
		}
		return s
	}

	paramTypeStr := cleanStr(data["parameterType"])
	var paramTypes []string
	if paramTypeStr != "" {
		paramTypes = strings.Split(paramTypeStr, ",")
	} else {
		paramTypes = []string{}
	}

	stmt := &entity.HttpStatement{
		InterfaceName: cleanStr(data["interfaceName"]),
		MethodName:    cleanStr(data["methodName"]),
		ParameterType: paramTypes,
		IsAuth:        isAuth,
		IsHttp:        isHttp,
		HttpType:      enum.HTTPType(cleanStr(data["httpType"])),
		ServiceId:     cleanStr(data["serviceId"]),
	}
	// 如果 ServiceId 为空，使用 InterfaceName
	if stmt.ServiceId == "" {
		stmt.ServiceId = stmt.InterfaceName
	}

	return stmt, nil
}
