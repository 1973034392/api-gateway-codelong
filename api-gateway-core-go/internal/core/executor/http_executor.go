package executor

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"net/url"
	"time"

	"api-gateway-core-go/internal/model/entity"
	"api-gateway-core-go/internal/model/enum"
)

type HttpExecutor struct {
	client *http.Client
}

var DefaultHttpExecutor *HttpExecutor

func InitHttpExecutor() {
	DefaultHttpExecutor = &HttpExecutor{
		client: &http.Client{
			Transport: &http.Transport{
				MaxIdleConns:        500,
				MaxIdleConnsPerHost: 50,
				IdleConnTimeout:     30 * time.Second,
				DialContext: (&net.Dialer{
					Timeout:   5 * time.Second, // 连接超时
					KeepAlive: 30 * time.Second,
				}).DialContext,
			},
			Timeout: 10 * time.Second, // 总请求超时
		},
	}
}

func (e *HttpExecutor) Execute(params map[string]interface{}, targetUrl string, stmt *entity.HttpStatement) (string, error) {
	var req *http.Request
	var err error

	switch stmt.HttpType {
	case enum.GET:
		reqUrl := targetUrl
		if len(params) > 0 {
			u, _ := url.Parse(targetUrl)
			q := u.Query()
			for k, v := range params {
				q.Set(k, fmt.Sprintf("%v", v))
			}
			u.RawQuery = q.Encode()
			reqUrl = u.String()
		}
		req, err = http.NewRequest(http.MethodGet, reqUrl, nil)

	case enum.POST:
		jsonBytes, _ := json.Marshal(params)
		req, err = http.NewRequest(http.MethodPost, targetUrl, bytes.NewBuffer(jsonBytes))
		req.Header.Set("Content-Type", "application/json")

	case enum.PUT:
		jsonBytes, _ := json.Marshal(params)
		req, err = http.NewRequest(http.MethodPut, targetUrl, bytes.NewBuffer(jsonBytes))
		req.Header.Set("Content-Type", "application/json")

	case enum.DELETE:
		reqUrl := targetUrl
		if len(params) > 0 {
			u, _ := url.Parse(targetUrl)
			q := u.Query()
			for k, v := range params {
				q.Set(k, fmt.Sprintf("%v", v))
			}
			u.RawQuery = q.Encode()
			reqUrl = u.String()
		}
		req, err = http.NewRequest(http.MethodDelete, reqUrl, nil)

	default:
		return "", fmt.Errorf("unsupported http type: %v", stmt.HttpType)
	}

	if err != nil {
		return "", err
	}

	resp, err := e.client.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", err
	}

	if resp.StatusCode >= 200 && resp.StatusCode < 300 {
		return string(bodyBytes), nil
	}

	return "", fmt.Errorf("http request failed, status: %d, body: %s", resp.StatusCode, string(bodyBytes))
}
