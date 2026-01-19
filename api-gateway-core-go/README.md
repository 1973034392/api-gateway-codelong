# API Gateway Core (Go Version)

这是 `api-gateway-core` 的 Go 语言重构版本，旨在提供更高并发的处理能力，同时保持原有业务逻辑的一致性。

## 功能特性

*   **高性能 Web 框架**: 基于 Gin，配合 Go 协程模型，轻松应对高并发。
*   **多级限流**: 支持全局、服务、接口、IP 四级限流，复用 Redis Lua 脚本，保证与 Java 版算法一致。
*   **安全认证**: 实现了基于 JWT 的鉴权机制。
*   **动态路由**: 支持从 Redis/网关中心 动态加载接口配置，并支持本地 LRU 缓存。
*   **服务发现**: 自动向 `api-gateway-center` 注册，并自动发现后端服务实例。
*   **HTTP 转发**: 高性能 HTTP 代理，支持连接池复用。

## 目录结构

*   `cmd/server`: 程序入口。
*   `config`: 配置加载 (Viper)。
*   `internal/core`: 核心逻辑。
    *   `middleware`: 鉴权、限流中间件。
    *   `handler`: 代理处理器。
    *   `executor`: HTTP 执行器。
    *   `ratelimiter`: 分布式限流器实现。
*   `internal/manager`: 路由与服务管理。
*   `internal/service`: 注册中心交互。

## 快速开始

1.  **环境准备**:
    *   Go 1.20+
    *   Redis
    *   启动 `api-gateway-center`

2.  **配置**:
    修改 `application.yml` 中的配置项（如 Redis 地址、网关中心地址）。

3.  **运行**:
    ```bash
    go mod tidy
    go run cmd/server/main.go
    ```

## 注意事项

*   目前主要实现了 HTTP 协议的转发。Dubbo 协议转发预留了接口，暂未实现。
*   请确保 Redis 中有正确的接口配置数据（通常由网关中心写入）。
