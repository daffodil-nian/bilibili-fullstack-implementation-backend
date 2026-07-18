package org.arrinna.bilibilimockbackground.common.algorithm;

import lombok.Data;

/**
 * @author Arrinnna
 * 标准的雪花算法结构是64bit long
 * 首先是符号位 0
 * 然后就是时间戳，41bit默认可以使用69年
 * 接下来就是机器ID，10bit可以支持1024台服务节点
 * 最后就是序列号，12bit每秒可以生成4096个ID
 * 4096*1000=409.6w，不存在用不完的情况
 */
@Data
public class SnowAlgorithm {
    private static final long START_TIMESTAMP = 1735689600000L;
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    // 外部传入机器ID
    public SnowAlgorithm(long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("机器ID范围必须是 0 ~ " + MAX_WORKER_ID);
        }
        this.workerId = workerId;
    }

    public synchronized long nextIdInstance() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            throw new RuntimeException("系统时钟回拨，禁止生成ID，回拨毫秒：" + offset);
        }
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMilli(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMilli(long lastTs) {
        long ts = System.currentTimeMillis();
        while (ts <= lastTs) {
            ts = System.currentTimeMillis();
        }
        return ts;
    }
}
