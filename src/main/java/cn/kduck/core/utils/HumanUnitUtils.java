package cn.kduck.core.utils;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HumanUnitUtils {

    private static final long ONE_DAY_SECONDS = 60 * 60 * 24;
    private static final long ONE_HOUR_SECONDS = 60 * 60;

    private static final String[] UNITS = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

    private HumanUnitUtils() {}

    public static String toHumanCapacity(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Bytes should be a positive value.");
        }

        if (bytes == 0) {
            return "0 B";
        }

        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        DecimalFormat decimalFormat = new DecimalFormat("0.##");
        return decimalFormat.format(bytes / Math.pow(1024, digitGroups)) + UNITS[digitGroups];
    }

    public static String toHumanTime(Date date) {
        return toHumanTime(date,10);
    }

    public static String toHumanTime(Date date,int maxDays) {
        long currentSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        long timeDifference = currentSeconds - TimeUnit.MILLISECONDS.toSeconds(date.getTime());

        long days = timeDifference / ONE_DAY_SECONDS;
        long hours = (timeDifference % ONE_DAY_SECONDS) / ONE_HOUR_SECONDS;
        long minutes = (timeDifference % ONE_HOUR_SECONDS) / 60;

        if(timeDifference < 30){
            return "刚刚";
        }else if(timeDifference < 60) {
            return "1分钟前";
        } else if (hours < 1) {
            return minutes + "分钟前";
        } else if (days < 1) {
            return hours + "小时前";
        } else if (days < maxDays) {
            return days + "天前";
        } else {
            return maxDays + "天以上";
        }
    }

}
