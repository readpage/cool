package cn.undraw.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author readpage
 * @date 2022-12-01 17:27
 */
public class DateTimeUtils {
    // 时区
//    private static String zoneId = "Asia/Shanghai";
    // 日期格式
    private static String dateFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 传日期字符串返回日期
     * @param dateTime
     * @return
     */
    public static LocalDateTime stringToDate(String dateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDateTime localDateTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
        return localDateTime;
    }

    /**
     *  传日期格式化为字符串
     * @param localDateTime
     * @return
     */
    public static String toString(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        String format = dateTimeFormatter.format(localDateTime);
        return format;
    }

    /**
     * date转换为localdatetime
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime;
    }

    /**
     * localDateTime转换为Date
     * @param localDateTime
     * @return
     */
    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * 毫秒数转换为localdatetime
     * @param time
     * @return
     */
    public static LocalDateTime longToLocalDateTime(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }

    /**
     * localdatetime转换为毫秒数
     * @param ldt
     * @return
     */
    public static long localDateTimeToLong(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
