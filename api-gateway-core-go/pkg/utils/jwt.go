package utils

import (
	"errors"
	"fmt"
	
	"api-gateway-core-go/config"

	"github.com/golang-jwt/jwt/v5"
)

func VerifyToken(tokenString string) (bool, error) {
	if tokenString == "" {
		return false, errors.New("token is empty")
	}

	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		// 验证签名算法
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		// 使用 SafeSecret 作为密钥
		return []byte(config.GlobalConfig.SafeSecret), nil
	})

	if err != nil {
		return false, err
	}

	if claims, ok := token.Claims.(jwt.MapClaims); ok && token.Valid {
		// 验证 safe-key
		if safeKey, ok := claims["safe-key"].(string); ok {
			if safeKey == config.GlobalConfig.SafeKey {
				return true, nil
			}
			return false, errors.New("safe-key mismatch")
		}
		return false, errors.New("safe-key claim missing")
	}

	return false, errors.New("invalid token")
}
