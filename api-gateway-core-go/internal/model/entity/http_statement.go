package entity

import "api-gateway-core-go/internal/model/enum"

type HttpStatement struct {
	InterfaceName string        `json:"interfaceName"`
	MethodName    string        `json:"methodName"`
	ParameterType []string      `json:"parameterType"`
	IsAuth        bool          `json:"isAuth"`
	IsHttp        bool          `json:"isHttp"`
	HttpType      enum.HTTPType `json:"httpType"`
	ServiceId     string        `json:"serviceId"`
}

func (h *HttpStatement) GetServiceId() string {
	if h.ServiceId != "" {
		return h.ServiceId
	}
	return h.InterfaceName
}
