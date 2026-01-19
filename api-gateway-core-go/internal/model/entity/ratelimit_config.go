package entity

type RateLimitConfig struct {
	ID             int64  `json:"id"`
	LimitType      string `json:"limitType"`   // "GLOBAL", "SERVICE", "INTERFACE", "IP"
	LimitTarget    string `json:"limitTarget"` // ServiceID, URL, IP
	Enabled        bool   `json:"enabled"`
	LimitCount     int    `json:"limitCount"`
	TimeWindow     int    `json:"timeWindow"` // 秒
	Strategy       string `json:"strategy"`   // "TOKEN_BUCKET" (令牌桶) 或 "SLIDING_WINDOW" (滑动窗口)
	Mode           string `json:"mode"`       // "DISTRIBUTED" (分布式) 或 "LOCAL_DISTRIBUTED" (本地+分布式)
	LocalBatchSize int    `json:"localBatchSize"`
}
