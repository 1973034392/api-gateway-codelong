package top.codelong.apigatewaycenter.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import top.codelong.apigatewaycenter.config.UniqueIdConfig;

@Component
public class UniqueIdUtil {
    private final StringRedisTemplate redisTemplate;
    private final UniqueIdConfig uniqueIdConfig;

    private final long nodeId;
    private final long startTimestamp; // 起始时间戳（毫秒）

    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    public UniqueIdUtil(StringRedisTemplate redisTemplate, UniqueIdConfig uniqueIdConfig) {
        this.redisTemplate = redisTemplate;
        this.uniqueIdConfig = uniqueIdConfig;
        this.startTimestamp = 1600000000000L; // 2020-09-13
        this.nodeId = initNodeId();
    }

    /**
     * 初始化节点ID（从Redis获取）
     */
    private long initNodeId() {
        String key = uniqueIdConfig.getNodeIdKey() + getApplicationName();
        // 使用Redis自增获取唯一节点ID
        Long nodeId = redisTemplate.opsForValue().increment(key);

        if (nodeId == null || nodeId > getMaxNodeId()) {
            throw new RuntimeException("无法获取有效节点ID");
        }

        // 设置过期时间（防止节点ID耗尽）
        redisTemplate.expire(key, 86400, java.util.concurrent.TimeUnit.SECONDS); // 1天
        return nodeId;
    }

    /**
     * 获取最大节点ID
     */
    private long getMaxNodeId() {
        return ~(-1L << uniqueIdConfig.getNodeIdBits());
    }

    /**
     * 获取应用名称（可从配置获取）
     */
    private String getApplicationName() {
        return "api-gateway:";
    }

    /**
     * 生成唯一ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 检查时钟回拨
        if (timestamp < lastTimestamp) {
            long clockBack = lastTimestamp - timestamp;
            if (clockBack > 5) { // 允许5ms的时钟回拨
                throw new RuntimeException("时钟回拨异常，回拨时间：" + clockBack + "ms");
            }
            try {
                Thread.sleep(clockBack << 1);
                timestamp = System.currentTimeMillis();
            } catch (InterruptedException e) {
                throw new RuntimeException("时钟回拨处理失败", e);
            }
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & uniqueIdConfig.getMaxSequence();
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        // 组装ID：时间戳 << (nodeIdBits + sequenceBits)
        //       | 节点ID << sequenceBits
        //       | 序列号
        return (timestamp - startTimestamp) << (uniqueIdConfig.getNodeIdBits() + uniqueIdConfig.getSequenceBits())
                | (nodeId << uniqueIdConfig.getSequenceBits())
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
