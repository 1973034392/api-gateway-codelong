package utils

import (
	"encoding/json"
	"io"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

func ParseParameters(c *gin.Context) (map[string]interface{}, error) {
	params := make(map[string]interface{})

	// 1. URL 查询参数
	for k, v := range c.Request.URL.Query() {
		if len(v) > 0 {
			params[k] = v[0] // 取第一个值
		}
	}

	// 2. 请求体
	method := c.Request.Method
	contentType := c.GetHeader("Content-Type")

	if method == http.MethodPost || method == http.MethodPut || method == http.MethodDelete {
		if contentType == "" || contentType == "none" {
			return params, nil
		}

		if strings.Contains(contentType, "application/json") {
			bodyBytes, err := io.ReadAll(c.Request.Body)
			if err != nil {
				return nil, err
			}
			if len(bodyBytes) > 0 {
				var jsonBody map[string]interface{}
				if err := json.Unmarshal(bodyBytes, &jsonBody); err != nil {
					return nil, err
				}
				for k, v := range jsonBody {
					params[k] = v
				}
			}
		} else if strings.Contains(contentType, "multipart/form-data") || strings.Contains(contentType, "application/x-www-form-urlencoded") {
			// 32MB 最大内存
			if err := c.Request.ParseMultipartForm(32 << 20); err != nil && err != http.ErrNotMultipart {
				// 如果不是 multipart，尝试 ParseForm
				if err := c.Request.ParseForm(); err != nil {
					return nil, err
				}
			}
			for k, v := range c.Request.Form {
				if len(v) > 0 {
					params[k] = v[0]
				}
			}
		}
	}

	return params, nil
}
