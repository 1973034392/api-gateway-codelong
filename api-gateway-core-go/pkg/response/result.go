package response

type Result[T any] struct {
	Code    int    `json:"code"`
	Message string `json:"msg"`
	Data    T      `json:"data"`
}

func (r *Result[T]) IsSuccess() bool {
	return r.Code == 200
}
