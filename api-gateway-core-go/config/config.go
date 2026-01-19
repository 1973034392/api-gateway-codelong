package config

import (
	"fmt"

	"github.com/spf13/viper"
)

type Config struct {
	ApiGateway ApiGatewayConfig `mapstructure:"api-gateway"`
	Redis      RedisConfig      `mapstructure:"spring"`
	ServerName string           // 注册后获取
	SafeKey    string           // 注册后获取
	SafeSecret string           // 注册后获取
}

type ApiGatewayConfig struct {
	Port          int    `mapstructure:"netty-port"`
	GatewayCenter string `mapstructure:"gateway-center"`
	GroupKey      string `mapstructure:"group-key"`
	Weight        int    `mapstructure:"weight"`
	MaxCache      int    `mapstructure:"max-cache"`
	BossThreads   int    `mapstructure:"boss-threads"`
	WorkerThreads int    `mapstructure:"worker-threads"`
}

type RedisConfig struct {
	Data struct {
		Redis struct {
			Host     string `mapstructure:"host"`
			Port     int    `mapstructure:"port"`
			Database int    `mapstructure:"database"`
		} `mapstructure:"redis"`
	} `mapstructure:"data"`
}

var GlobalConfig Config

func InitConfig() {
	viper.SetConfigName("application")
	viper.SetConfigType("yml")
	viper.AddConfigPath(".")
	viper.AddConfigPath("./config")

	// 设置默认值
	viper.SetDefault("api-gateway.netty-port", 8888)
	viper.SetDefault("api-gateway.weight", 1)
	viper.SetDefault("api-gateway.max-cache", 1000)
	viper.SetDefault("api-gateway.boss-threads", 1)
	viper.SetDefault("api-gateway.worker-threads", 4)
	viper.SetDefault("spring.data.redis.host", "localhost")
	viper.SetDefault("spring.data.redis.port", 6379)
	viper.SetDefault("spring.data.redis.database", 0)

	if err := viper.ReadInConfig(); err != nil {
		fmt.Printf("Error reading config file, %s\n", err)
	}

	if err := viper.Unmarshal(&GlobalConfig); err != nil {
		fmt.Printf("Unable to decode into struct, %v\n", err)
	}
}
