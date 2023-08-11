package cn.undraw.util.snowflake;

/**
 * @author readpage
 * @date 2023-07-05 00:00
 */
public class Snowflake {
    // 开始时间戳，一般为项目启动时间
    private final long twepoch = 1688486400000L;
    // 机器ID所占的位数
    private final long workerIdBits = 5L;
    // 数据标识ID所占的位数
    private final long datacenterIdBits = 5L;
    // 支持的最大机器ID，结果是31
    private final long maxWorkerId = ~(-1L << workerIdBits);
    // 支持的最大数据标识ID，结果是31
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);
    // 序列号所占的位数
    private final long sequenceBits = 12L;
    // 机器ID向左移12位
    private final long workerIdShift = sequenceBits;
    // 数据标识ID向左移17位(12+5)
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    // 时间戳向左移22位(5+5+12)
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    // 生成序列的掩码，这里是4095
    private final long sequenceMask = ~(-1L << sequenceBits);

    private long workerId; // 机器ID
    private long datacenterId; // 数据标识ID
    private long sequence = 0L; // 序列号
    private long lastTimestamp = -1L; // 上次生成ID的时间戳

    public Snowflake(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask; // 序列号自增
            if (sequence == 0) { // 序列号超过最大值，则等待下一个时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L; // 序列号重置为0
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }

}
