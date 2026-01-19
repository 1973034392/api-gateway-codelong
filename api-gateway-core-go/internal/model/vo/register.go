package vo

type GroupRegisterReqVO struct {
	GroupKey      string `json:"groupKey"`
	DetailName    string `json:"detailName"`
	DetailAddress string `json:"detailAddress"`
	DetailWeight  int    `json:"detailWeight"`
}

type GroupDetailRegisterRespVO struct {
	ServerName string `json:"serverName"`
	SafeKey    string `json:"safeKey"`
	SafeSecret string `json:"safeSecret"`
}
