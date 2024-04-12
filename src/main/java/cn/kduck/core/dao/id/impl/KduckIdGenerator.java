package cn.kduck.core.dao.id.impl;

import cn.kduck.core.dao.id.IdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

public class KduckIdGenerator implements IdGenerator {

    private final Log logger = LogFactory.getLog(getClass());

    private int timeBits = 31;
    private int ipRegionBits = 3;
    private int ipSegmentBits = 8;
    private int seqBits = 13;

    private long maxDeltaSeconds = -1L ^ (-1L << timeBits);
    private long maxSequence = -1L ^ (-1L << seqBits);

    private long epochSeconds = TimeUnit.MILLISECONDS.toSeconds(1287590400000L);

    private long secondsShift = ipRegionBits + ipSegmentBits + ipSegmentBits + seqBits;

    private long ipRegionShift = ipSegmentBits + ipSegmentBits + seqBits;

    private long ipShift = ipSegmentBits + seqBits;

    private long sequence = 0L;
    private long lastSecond = -1L;

    private final int[] ipAddress = new int[4];

    private final int reginValue;

    public KduckIdGenerator(){
        this(null);
    }

    public KduckIdGenerator(String ip){
        this(ip,new String[0]);
    }

    public KduckIdGenerator(String ip, String[] regions){
        this(ip,ipAddress->{
            if(regions == null || regions.length == 0){
                return 0;
            }

            for (int i = 0; i < regions.length; i++) {
                if(ipAddress.startsWith(regions[i])){
                    return i;
                }
            }
            throw new RuntimeException("当前IP未匹配到任何区域：ip=" + ipAddress + ",regions=" + Arrays.toString(regions));
        });

    }

    public KduckIdGenerator(String ip, ReginAllocator reginAllocator){

        this.reginValue = reginAllocator.allot(ip);

        if(ip == null || ip.length() == 0){
            byte[] address = getLocalIpAddress();

            for(int i = 0 ; i < ipAddress.length ; i++){
                ipAddress[i] = address[i] & 0xff;
            }
        }else{
            String[] ipv4Address = ip.split("[.]");
            if(ipv4Address.length != 4){
                throw new RuntimeException("IP地址不合法，请提供一个正确的IPv4的IP地址："+ipAddress);
            }
            for(int i = 0 ; i < ipAddress.length ; i++){
                ipAddress[i] = Integer.parseInt(ipv4Address[i]);
            }
        }
        if(logger.isInfoEnabled()){
            String ipStr = ipAddress[0] + "." + ipAddress[1] + "." + ipAddress[2] + "." + ipAddress[3];
            logger.info("基于IP的主键生成器，提取的IP地址为：" + ipStr);
        }

    }

    public byte[] getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (!address.isLinkLocalAddress() && address instanceof Inet4Address) {
                        return address.getAddress();
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("初始化生成器错误，无法获取指定主机的IP地址", e);
        }

        return null; // 如果没有找到有效的IPv4地址，则返回null
    }

    @Override
    public synchronized Serializable nextId() {
        long currentSecond = getCurrentSecond();

        if (currentSecond < lastSecond) {
            long refusedSeconds = lastSecond - currentSecond;
            throw new RuntimeException("Clock moved backwards. Refusing for " + refusedSeconds + " seconds");
        }

        if (currentSecond == lastSecond) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                currentSecond = getNextSecond(lastSecond);
            }
        } else {
            sequence = 0L;
        }

        lastSecond = currentSecond;

        return ((currentSecond - epochSeconds) << secondsShift) | (reginValue << ipRegionShift) | (ipAddress[2] << ipShift) | (ipAddress[3] << seqBits) | sequence;
    }

    private long getNextSecond(long lastSecond) {
        long second = getCurrentSecond();
        while (second <= lastSecond) {
            second = getCurrentSecond();
        }

        return second;
    }

    protected long getCurrentSecond() {
        long currentSecond = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (currentSecond - epochSeconds > maxDeltaSeconds) {
            throw new RuntimeException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentSecond);
        }

        return currentSecond;
    }

    public interface ReginAllocator{
        int allot(String ipAddress);
    }

    @Component
    @ConfigurationProperties("kduck.id.kduck-id")
    public static class KduckSnowFlakeProperties {
        private String serverIp;
        private String[] regions;

        public String getServerIp() {
            return serverIp;
        }

        public void setServerIp(String serverIp) {
            this.serverIp = serverIp;
        }

        public String[] getRegions() {
            return regions;
        }

        public void setRegions(String[] regions) {
            this.regions = regions;
        }
    }

}
