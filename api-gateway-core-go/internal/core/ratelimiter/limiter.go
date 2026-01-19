package ratelimiter

import (
	"context"
	"time"
	"sync"
	"sync/atomic"

	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/internal/core/ratelimiter/lua"
	"api-gateway-core-go/pkg/logger"

	"github.com/redis/go-redis/v9"
	"go.uber.org/zap"
)

type DistributedRateLimiter struct {
	redisClient *redis.Client
	// map[string]*entity.RateLimitConfig
	configCache sync.Map 
	// map[string]*atomic.Int64
	localTokenCounters sync.Map
}

var Limiter *DistributedRateLimiter

func InitRateLimiter(client *redis.Client) {
	Limiter = &DistributedRateLimiter{
		redisClient: client,
	}
}

func (l *DistributedRateLimiter) UpdateConfig(key string, config *entity.RateLimitConfig) {
	l.configCache.Store(key, config)
	l.localTokenCounters.Delete(key) // 重置计数器
}

func (l *DistributedRateLimiter) GetConfig(key string) *entity.RateLimitConfig {
	if val, ok := l.configCache.Load(key); ok {
		return val.(*entity.RateLimitConfig)
	}
	return nil
}

func (l *DistributedRateLimiter) TryAcquire(key string, config *entity.RateLimitConfig) bool {
	if config == nil || !config.Enabled {
		return true
	}

	mode := config.Mode
	if mode == "" {
		mode = "DISTRIBUTED"
	}

	if mode == "LOCAL_DISTRIBUTED" {
		return l.tryAcquireLocalDistributed(key, config)
	}
	return l.tryAcquireDistributed(key, config)
}

func (l *DistributedRateLimiter) ClearCache() {
	l.configCache.Range(func(key, value interface{}) bool {
		l.configCache.Delete(key)
		return true
	})
	l.localTokenCounters.Range(func(key, value interface{}) bool {
		l.localTokenCounters.Delete(key)
		return true
	})
}

func (l *DistributedRateLimiter) tryAcquireDistributed(key string, config *entity.RateLimitConfig) bool {
	redisKey := "rate_limit:" + key
	ctx := context.Background()

	var err error
	var res interface{}

	if config.Strategy == "TOKEN_BUCKET" {
		res, err = l.redisClient.Eval(ctx, lua.TokenBucketScript, []string{redisKey}, config.LimitCount).Result()
	} else {
		// 滑动窗口
		now := time.Now().UnixNano() / 1e6 // ms
		res, err = l.redisClient.Eval(ctx, lua.SlidingWindowScript, []string{redisKey}, config.LimitCount, config.TimeWindow, now).Result()
	}

	if err != nil {
		logger.Error("限流脚本执行失败", zap.String("key", key), zap.Error(err))
		return true // 降级
	}

	return res.(int64) == 1
}

func (l *DistributedRateLimiter) tryAcquireLocalDistributed(key string, config *entity.RateLimitConfig) bool {
	// 1. 获取或创建本地计数器
	v, _ := l.localTokenCounters.LoadOrStore(key, &atomic.Int64{})
	counter := v.(*atomic.Int64)

	// 2. 尝试本地扣减
	current := counter.Load()
	if current > 0 {
		if counter.CompareAndSwap(current, current-1) {
			return true
		}
		// 重试一次
		return l.tryAcquireLocalDistributed(key, config)
	}

	// 3. 从 Redis 批量获取
	batchSize := config.LocalBatchSize
	if batchSize <= 0 {
		batchSize = 100
	}
	
	acquired := l.batchGetTokensFromRedis(key, config, batchSize)
	if acquired > 0 {
		counter.Store(int64(acquired - 1))
		return true
	}

	return false
}

func (l *DistributedRateLimiter) batchGetTokensFromRedis(key string, config *entity.RateLimitConfig, batchSize int) int {
	redisKey := "rate_limit:" + key
	res, err := l.redisClient.Eval(context.Background(), lua.BatchGetTokensScript, []string{redisKey}, batchSize, config.LimitCount).Result()
	if err != nil {
		logger.Error("批量获取令牌失败", zap.String("key", key), zap.Error(err))
		return 0
	}
	return int(res.(int64))
}
