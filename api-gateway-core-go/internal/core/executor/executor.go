package executor

import "api-gateway-core-go/internal/model/entity"

type Executor interface {
	Execute(params map[string]interface{}, url string, stmt *entity.HttpStatement) (string, error)
}
