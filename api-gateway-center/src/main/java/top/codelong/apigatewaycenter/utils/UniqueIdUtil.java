package top.codelong.apigatewaycenter.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.config.UniqueIdConfig;

/**
 * 分布式唯一ID生成工具类
 * 基于Snowflake算法实现，使用Redis分配节点ID
 */
@Slf4j
@Component
public class UniqueIdUtil {
    private final StringRedisTemplate redisTemplate;
    private final UniqueIdConfig uniqueIdConfig;

    private final long nodeId;
    private final long startTimestamp; // 起始时间戳（毫秒）

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    /**
     * 构造函数
     * @param redisTemplate Redis模板
     * @param uniqueIdConfig ID生成配置
     */
    public UniqueIdUtil(StringRedisTemplate redisTemplate, UniqueIdConfig uniqueIdConfig) {
        this.redisTemplate = redisTemplate;
        this.uniqueIdConfig = uniqueIdConfig;
        this.startTimestamp = 1600000000000L; // 2020-09-13
        this.nodeId = initNodeId();
        log.info("UniqueIdUtil初始化完成，节点ID: {}", nodeId);
    }

    /**
     * 初始化节点ID（从Redis获取）
     * @return 分配的节点ID
     * @throws RuntimeException 当无法获取有效节点ID时抛出
     */
    private long initNodeId() {
        log.debug("开始初始化节点ID");
        String key = uniqueIdConfig.getNodeIdKey() + ':' + getApplicationName();

        // 使用Redis自增获取唯一节点ID
        Long nodeId = redisTemplate.opsForValue().increment(key);
        log.debug("从Redis获取的节点ID: {}", nodeId);

        if (nodeId == null || nodeId > getMaxNodeId()) {
            log.error("无法获取有效节点ID，当前值: {}, 最大值: {}", nodeId, getMaxNodeId());
            throw new RuntimeException("无法获取有效节点ID");
        }

        // 设置过期时间（防止节点ID耗尽）
        redisTemplate.expire(key, 86400, java.util.concurrent.TimeUnit.SECONDS); // 1天
        log.info("成功初始化节点ID: {}", nodeId);
        return nodeId;
    }

    /**
     * 获取最大节点ID
     * @return 最大节点ID值
     */
    private long getMaxNodeId() {
        long maxId = ~(-1L << uniqueIdConfig.getNodeIdBits());
        log.trace("计算最大节点ID: {}", maxId);
        return maxId;
    }

    /**
     * 获取应用名称（可从配置获取）
     * @return 应用名称
     */
    private String getApplicationName() {
        return "api-gateway:";
    }

    /**
     * 生成唯一ID
     * @return 生成的唯一ID（字符串格式，避免 JavaScript 精度丢失）
     * @throws RuntimeException 当时钟回拨异常时抛出
     */
    public synchronized String nextId() {
        log.debug("开始生成唯一ID");
        long timestamp = System.currentTimeMillis();

        // 检查时钟回拨
        if (timestamp < lastTimestamp) {
            long clockBack = lastTimestamp - timestamp;
            log.warn("检测到时钟回拨，回拨时间: {}ms", clockBack);

            if (clockBack > 5) { // 允许5ms的时钟回拨
                log.error("时钟回拨超过阈值，回拨时间: {}ms", clockBack);
                throw new RuntimeException("时钟回拨异常，回拨时间：" + clockBack + "ms");
            }

            try {
                Thread.sleep(clockBack << 1);
                timestamp = System.currentTimeMillis();
                log.debug("时钟回拨处理完成，新时间戳: {}", timestamp);
            } catch (InterruptedException e) {
                log.error("时钟回拨处理失败", e);
                throw new RuntimeException("时钟回拨处理失败", e);
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & uniqueIdConfig.getMaxSequence();
            log.trace("同一时间戳内序列号递增: {}", sequence);

            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
                log.debug("序列号溢出，等待下一毫秒: {}", timestamp);
            }
        } else {
            sequence = 0;
            log.trace("新时间戳，重置序列号");
        }

        lastTimestamp = timestamp;

        // 组装ID：时间戳 << (nodeIdBits + sequenceBits)
        //       | 节点ID << sequenceBits
        //       | 序列号
        long id = (timestamp - startTimestamp) << (uniqueIdConfig.getNodeIdBits() + uniqueIdConfig.getSequenceBits())
                | (nodeId << uniqueIdConfig.getSequenceBits())
                | sequence;

        log.debug("成功生成唯一ID: {}", id);
        return String.valueOf(id);
    }

    /**
     * 等待下一毫秒
     * @param lastTimestamp 最后时间戳
     * @return 新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        log.trace("开始等待下一毫秒，最后时间戳: {}", lastTimestamp);
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        log.trace("等待完成，新时间戳: {}", timestamp);
        return timestamp;
    }
}