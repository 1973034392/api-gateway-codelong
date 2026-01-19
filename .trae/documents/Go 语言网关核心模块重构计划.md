# 重构计划：Go 语言网关核心模块

我已详细分析了 Java 版 `api-gateway-core` 的代码和架构文档。该项目基于 Netty 实现了一个高性能网关，包含**接入、鉴权、限流、转发（HTTP/Dubbo）、结果处理**等核心环节。

基于用户“保持整体逻辑一致”且“考虑高并发能力”的要求，我制定了以下 Go 语言重构计划：

## 1. 核心架构与技术选型

为了在保持逻辑一致的同时发挥 Go 的高并发优势，我们将采用以下技术栈：

*   **Web 框架**: **Gin**
    *   *理由*: 虽有更底层的 `fasthttp`，但 `Gin` 在性能与生态间取得了最佳平衡，且配合 Go 的协程模型足以应对极高并发。它的 Middleware 机制完美对应 Netty 的 Handler 链。
*   **并发模型**: **Goroutine + Channel**
    *   *优化*: Go 原生的非阻塞 I/O 模型。对于极高频的对象创建，将使用 `sync.Pool` 进行复用以降低 GC 压力。
*   **配置中心/注册**: **Go 原生 HTTP Client + Viper**
    *   *逻辑*: 保持与 `api-gateway-center` 的交互逻辑一致（注册、心跳、配置拉取）。
*   **限流组件**: **go-redis/v9 + Lua 脚本**
    *   *逻辑*: 复用 Java 版的 Lua 脚本，确保多语言环境下的限流算法一致性。本地限流使用 `golang.org/x/time/rate`。
*   **RPC 支持**: **Dubbo-go (v3.0)**
    *   *逻辑*: 支持 Dubbo 泛化调用。
*   **缓存**: **BigCache (本地)** + **Redis (分布式)**
    *   *逻辑*: 实现多级缓存策略，L1 为本地内存（纳秒级），L2 为 Redis。

## 2. 模块映射 (Java -> Go)

我们将把 Java 的 Netty Pipeline 转换为 Go 的 Middleware Chain：

| Java 组件 (Netty Handler) | Go 组件 (Gin Middleware/Handler) | 功能描述 |
| :--- | :--- | :--- |
| `SocketServerBootStrap` | `cmd/server/main.go` | 程序入口，启动 HTTP Server |
| `AuthorizationHandler` | `middleware/AuthMiddleware` | **鉴权**：解析 JWT，校验 `safeKey` |
| `RateLimitHandler` | `middleware/RateLimitMiddleware` | **限流**：执行全局/服务/接口/IP 四级限流 |
| `RequestParameterUtil` | `utils/ParamParser` | **参数解析**：处理 JSON/Form/Query 参数 |
| `DefaultHTTPExecutor` | `executor/HttpExecutor` | **转发**：异步 HTTP Client (复用连接池) |
| `DefaultDubboExecutor` | `executor/DubboExecutor` | **转发**：Dubbo 泛化调用 |
| `ResultHandler` | `handler/ResponseHandler` | **响应**：结果封装与缓存 |
| `GlobalConfiguration` | `config/Config` & `service/Register` | **配置**：服务注册与配置管理 |

## 3. 目录结构设计

```text
api-gateway-core-go/
├── cmd/server/           # 入口
├── config/               # 配置定义 (Viper)
├── internal/
│   ├── core/
│   │   ├── middleware/   # 鉴权、限流中间件
│   │   └── executor/     # HTTP/Dubbo 执行器
│   ├── manager/          # 路由规则与连接池管理
│   ├── service/          # 注册中心交互逻辑
│   └── model/            # VO 和实体定义
├── pkg/utils/            # JWT、IP 等工具类
└── go.mod
```

## 4. 高并发优化策略

1.  **连接池调优**: 对 `http.Client` 和 Redis Client 进行精细化配置（`MaxIdleConns`, `MaxConnsPerHost`），避免连接泄露和频繁握手。
2.  **零拷贝优化**: 在参数转发过程中，尽量减少 `[]byte` 到 `string` 的转换。
3.  **配置热更新**: 使用 Redis Pub/Sub 监听配置变更，实时更新本地 `BigCache`，避免高并发下频繁读取 Redis 导致热 Key 问题。

## 5. 执行步骤

1.  **初始化**: 搭建项目骨架，配置 Viper 和 Zap 日志。
2.  **核心服务**: 实现 `RegisterService`，完成与 Java 网关中心的对接（注册、获取密钥）。
3.  **中间件开发**: 移植鉴权 (`Auth`) 和限流 (`RateLimit`) 逻辑。
4.  **执行器实现**: 实现 HTTP 转发逻辑（优先），随后实现 Dubbo。
5.  **整合验证**: 组装 Pipeline，进行端到端测试。

准备就绪后，我将从项目初始化开始执行。
