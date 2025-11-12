-- 网关限流配置表
CREATE TABLE IF NOT EXISTS `gateway_rate_limit` (
    `id` BIGINT NOT NULL COMMENT '唯一id',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '限流规则名称',
    `limit_type` VARCHAR(20) NOT NULL COMMENT '限流类型：GLOBAL(全局)、SERVICE(服务级)、INTERFACE(接口级)、IP(IP级)',
    `limit_target` VARCHAR(200) NOT NULL COMMENT '限流目标：服务名、接口URL、IP地址等',
    `limit_count` INT NOT NULL COMMENT '限流阈值（每秒请求数）',
    `time_window` INT NOT NULL DEFAULT 1 COMMENT '时间窗口（秒）',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `strategy` VARCHAR(20) NOT NULL DEFAULT 'TOKEN_BUCKET' COMMENT '限流策略：TOKEN_BUCKET(令牌桶)、SLIDING_WINDOW(滑动窗口)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_limit_type` (`limit_type`),
    KEY `idx_limit_target` (`limit_target`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网关限流配置表';

-- 插入示例数据
INSERT INTO `gateway_rate_limit` (`id`, `rule_name`, `limit_type`, `limit_target`, `limit_count`, `time_window`, `status`, `strategy`) VALUES
(1, '全局限流', 'GLOBAL', 'GLOBAL', 10000, 1, 1, 'TOKEN_BUCKET'),
(2, '测试服务限流', 'SERVICE', 'test-service', 1000, 1, 1, 'TOKEN_BUCKET'),
(3, '登录接口限流', 'INTERFACE', 'test-service:/api/login', 100, 1, 1, 'SLIDING_WINDOW'),
(4, 'IP限流', 'IP', '192.168.1.100', 50, 1, 1, 'TOKEN_BUCKET');

