package top.codelong.apigatewaycenter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "unique-id")
public class UniqueIdConfig {
    private long nodeIdBits;      // 节点ID位数
    private long sequenceBits;    // 序列号位数
    private long maxSequence = ~(-1L << sequenceBits); // 序列号最大值
    private String nodeIdKey;
}
