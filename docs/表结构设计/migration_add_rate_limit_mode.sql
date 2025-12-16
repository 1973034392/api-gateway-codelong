-- ============================================================================
-- 数据库迁移脚本：为 gateway_rate_limit 表添加新的限流模式字段
-- ============================================================================
-- 执行时间：2024年
-- 说明：本脚本为已有的 gateway_rate_limit 表添加三个新字段，支持新的限流模式
-- ============================================================================

-- 1. 添加限流模式字段 (mode)
ALTER TABLE gateway_rate_limit
ADD COLUMN mode VARCHAR(20) DEFAULT 'DISTRIBUTED' NOT NULL COMMENT '限流模式：DISTRIBUTED(分布式)、LOCAL_DISTRIBUTED(本地+分布式混合)' AFTER strategy;

-- 2. 添加本地批量获取令牌数字段 (local_batch_size)
ALTER TABLE gateway_rate_limit
ADD COLUMN local_batch_size INT DEFAULT 100 NOT NULL COMMENT '本地批量获取令牌数（仅在LOCAL_DISTRIBUTED模式下使用）' AFTER mode;

-- 3. 添加本地容量倍数字段 (local_capacity_multiplier)
ALTER TABLE gateway_rate_limit
ADD COLUMN local_capacity_multiplier DOUBLE DEFAULT 1.0 NOT NULL COMMENT '本地容量倍数（仅在LOCAL_DISTRIBUTED模式下使用）' AFTER local_batch_size;

-- ============================================================================
-- 验证脚本（执行完成后可运行以下命令验证）
-- ============================================================================
-- DESC gateway_rate_limit;
-- SELECT * FROM gateway_rate_limit LIMIT 1;

-- ============================================================================
-- 回滚脚本（如需回滚，执行以下命令）
-- ============================================================================
-- ALTER TABLE gateway_rate_limit DROP COLUMN mode;
-- ALTER TABLE gateway_rate_limit DROP COLUMN local_batch_size;
-- ALTER TABLE gateway_rate_limit DROP COLUMN local_capacity_multiplier;

