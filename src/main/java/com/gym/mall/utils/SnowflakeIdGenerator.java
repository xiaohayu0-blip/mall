package com.gym.mall.utils;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 雪花算法 ID 生成器
 *
 * 结构：1bit(0) | 41bit(时间戳) | 10bit(工作节点) | 12bit(序列号)
 * - 时间戳：相对于 2023-01-01 的毫秒偏移量，可用约 69 年
 * - 工作节点：支持 1024 个节点
 * - 序列号：每毫秒可生成 4096 个 ID
 *
 * 知识点：分布式ID、位运算、时钟回拨
 */
@Component
public class SnowflakeIdGenerator {

    /** 起始时间戳：2023-01-01 00:00:00 UTC */
    private static final long START_EPOCH = 1672531200000L;

    /** 工作节点 ID 占用的位数 */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号占用的位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 最大工作节点 ID（1023） */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 最大序列号（4095） */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 工作节点 ID 左移位数（12） */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 时间戳左移位数（22 = 10 + 12） */
    private static final long TIMESTAMP_LEFT_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    /** 工作节点 ID（可通过配置注入） */
    private long workerId;

    /** 上次生成 ID 的时间戳 */
    private long lastTimestamp = -1L;

    /** 当前毫秒的序列号 */
    private long sequence = 0L;

    public SnowflakeIdGenerator() {
        this.workerId = 1L; // 默认节点 ID，单机部署下固定即可
    }

    public SnowflakeIdGenerator(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("工作节点 ID 必须在 0 ~ " + MAX_WORKER_ID + " 之间");
        }
        this.workerId = workerId;
    }

    @PostConstruct
    public void init() {
        if (this.workerId < 0 || this.workerId > MAX_WORKER_ID) {
            this.workerId = 1L;
        }
    }

    /**
     * 生成下一个全局唯一 ID
     *
     * @return 64 位 long 类型的唯一 ID
     */
    public synchronized long nextId() {
        long currentTimestamp = timestamp();

        // 处理时钟回拨
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            // 如果回拨时间较短（< 10ms），等待到原时间戳
            if (offset < 10) {
                currentTimestamp = waitUntilNextMillis(lastTimestamp);
            } else {
                // 回拨过大，抛异常
                throw new IllegalStateException("时钟回拨幅度过大: " + offset + "ms");
            }
        }

        // 同一毫秒内，序列号自增
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 当前毫秒序列号用完，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = waitUntilNextMillis(currentTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;

        // 组装 64 位 ID
        return ((currentTimestamp - START_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long timestamp() {
        return System.currentTimeMillis();
    }

    private long waitUntilNextMillis(long lastTimestamp) {
        long currentTimestamp;
        do {
            currentTimestamp = timestamp();
        } while (currentTimestamp <= lastTimestamp);
        return currentTimestamp;
    }
}
