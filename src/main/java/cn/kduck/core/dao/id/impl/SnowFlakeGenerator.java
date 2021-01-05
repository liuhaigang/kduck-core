package cn.kduck.core.dao.id.impl;

import cn.kduck.core.dao.id.IdGenerator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * https://www.jianshu.com/p/2aa82d70c99b
 * https://www.sohu.com/a/232008315_453160
 * @author LiuHG
 */
public class SnowFlakeGenerator implements IdGenerator {
    /**
     * 机器号
     */
    private long workerId;
    /**
     * 数据中心号
     */
    private long datacenterId;
    /**
     * 同毫秒内自增序列号
     */
    private long sequence;
    /**
     * 程序序列号 第一次生成时间 可以自己配置
     */
    private long twepoch = 1287590400000L;
    /**
     * 机器号 5位
     */
    private long workerIdBits = 5L;
    /**
     * 数据中心号 5位
     */
    private long datacenterIdBits = 5L;
    /**
     * 最大机器号
     */
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    /**
     * 最大数据中心号
     */
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
    /**
     * 同毫秒内 自增序列位数
     */
    private long sequenceBits = 12L;
    /**
     * 机器号左移位数
     */
    private long workerIdShift = sequenceBits;
    /**
     * 数据中心号左移位数
     */
    private long datacenterIdShift = sequenceBits + workerIdBits;
    /**
     * 时间戳差值左移位数
     */
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    /**
     * 同毫秒内自增序列号最大值   防止溢出 影响机器号的值
     */
    private long sequenceMask = -1L ^ (-1L << sequenceBits);
    /**
     * 上次生成序列号的时间
     */
    private long lastTimestamp = -1L;

    /**
     *
     * @param workerId 工作机器号ID
     * @param datacenterId 数据中心ID
     * @param sequence 起始自增序列
     */
    public SnowFlakeGenerator(long workerId, long datacenterId, long sequence) {
        // sanity check for workerId
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
        }
        System.out.printf(
                "worker starting. timestamp left shift %d, datacenter id bits %d, worker id bits %d, sequence bits %d, workerid %d",
                timestampLeftShift, datacenterIdBits, workerIdBits, sequenceBits, workerId);

        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }


    public long getWorkerId() {
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public synchronized Serializable nextId() {
        /**获取当前时间*/
        long timestamp = timeGen();
        /**检查时间是否倒退*/
        if (timestamp < lastTimestamp) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp);
            throw new RuntimeException(String.format(
                    "Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        //如果本次生成时间跟上次时间相同 那么自增序列增加，如果溢出那么就等下个时间，主要是防止重复
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //获取下个时间
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        //更新上次生成时间
        lastTimestamp = timestamp;
        //          将4部分合在一起
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


    @Component
    @ConfigurationProperties("kduck.id.snow-flake")
    public static class SnowFlakeProperties {
        private Integer workerId = 0;
        private Integer dataCenterId = 0;
        private Integer sequence = 1;

        public Integer getWorkerId() {
            return workerId;
        }

        public void setWorkerId(Integer workerId) {
            this.workerId = workerId;
        }

        public Integer getDataCenterId() {
            return dataCenterId;
        }

        public void setDataCenterId(Integer dataCenterId) {
            this.dataCenterId = dataCenterId;
        }

        public Integer getSequence() {
            return sequence;
        }

        public void setSequence(Integer sequence) {
            this.sequence = sequence;
        }
    }
}