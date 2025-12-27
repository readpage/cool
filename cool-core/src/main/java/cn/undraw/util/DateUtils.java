package cn.undraw.util;

import cn.undraw.handler.exception.customer.CustomerException;
import lombok.Data;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-12-01 17:27
 */
public class DateUtils {
    // 时区
//    private static String zoneId = "Asia/Shanghai";
    // 日期格式
    public static String DATE_FORMAT = "yyyy-MM-dd";

    public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 自定义日期时间格式 yyyy-MM-dd HH:mm:ss
     * @param temporal LocalDate || LocalDateTime
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String format(TemporalAccessor temporal, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(temporal);
    }

    public static String format(String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(LocalDateTime.now());
    }

    /**
     * 传日期时间字符串返回日期时间
     * @param dateTime
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime toDateTime(String dateTime, String pattern) {
        if (StrUtils.isEmpty(dateTime)) return null;
        if (StrUtils.isNumber(dateTime)) {
            return DateUtils.toDateTime(Long.parseLong(dateTime));
        }
        try {
            if (pattern == null) {
                if (dateTime.matches("^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
                    return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"));
                }
                return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
            }

            return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            throw new CustomerException("时间参数[" + dateTime + "]类型转换异常");
        }
    }

    /**
     * 传日期时间字符串返回日期时间
     * @param dateTime
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime toDateTime(String dateTime) {
        return toDateTime(dateTime, null);
    }


    /**
     * date转换为localdatetime
     * @param date
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime toDateTime(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return localDateTime;
    }

    /**
     * 毫秒数转换为localdatetime
     * @param time
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime toDateTime(long time) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
    }


    /**
     * 传日期时间格式化为字符串 yyyy-MM-dd HH:mm:ss
     * @param localDateTime
     * @return java.lang.String
     */
    public static String toString(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        String format = dateTimeFormatter.format(localDateTime);
        return format;
    }

    /**
     * 返回LocalDateTime当前时间，并格式化为yyyy-MM-dd HH:mm:ss
     * @return java.lang.String
     */
    public static String now() {
        return toString(LocalDateTime.now());
    }

    /**
     * 传日期字符串返回日期
     * @param date
     * @return java.time.LocalDate
     */
    public static LocalDate toLocalDate(String date) {
        if (StrUtils.isEmpty(date)) return null;
        if (StrUtils.isNumber(date)) {
            return DateUtils.toDateTime(Long.parseLong(date)).toLocalDate();
        }
        try {
            Map<String, String> map = new HashMap<>();
            map.put("^\\d{4}/\\d{2}/\\d{2}$", "yyyy/MM/dd");
            map.put("^\\d{4}/\\d{1}/\\d{2}$", "yyyy/M/dd");
            map.put("^\\d{4}/\\d{2}/\\d{1}$", "yyyy/MM/d");
            map.put("^\\d{4}/\\d{1}/\\d{1}$", "yyyy/M/d");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (date.matches(entry.getKey())) {
                    return LocalDate.parse(date, DateTimeFormatter.ofPattern(entry.getValue()));
                }
            }
            return LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (Exception e) {
            throw new CustomerException("日期类型转换异常");
        }
    }

    /**
     * date转换为localDate
     * @param date
     * @return java.time.LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return toDateTime(date).toLocalDate();
    }

    /**
     * 毫秒数转换为localDate
     * @param time
     * @return java.time.LocalDate
     */
    public static LocalDate toLocalDate(long time) {
        return toDateTime(time).toLocalDate();
    }

    /**
     * localDateTime转换为Date
     * @param localDateTime
     * @return java.util.Date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    /**
     * localDate转换为date
     * @param localDate
     * @return java.util.Date
     */
    public static Date toDate(LocalDate localDate) {
        return toDate(localDate.atStartOfDay());
    }

    /**
     * 返回当前时间戳(ms/毫秒)
     * @return long
     */
    @Deprecated
    public static long toLong()  {
        return toLong(LocalDateTime.now());
    }

    /**
     * localDate转毫秒数
     * @param localDate
     * @return long
     */
    @Deprecated
    public static long toLong(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        return localDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
    }

    /**
     * 返回当前时间戳(ms/毫秒)
     * @return long
     */
    public static long toMilli()  {
        return toLong(LocalDateTime.now());
    }

    /**
     * localDate转毫秒数
     * @param localDate
     * @return long
     */
    public static long toMilli(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        return localDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
    }

    /**
     * localdatetime转换为毫秒数
     * @param localDateTime
     * @return long
     */
    public static long toMilli(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * localDate转秒数
     * @param localDate
     * @return long
     */
    public static long toSecond(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        return localDate.atStartOfDay(zoneId).toEpochSecond();
    }

    /**
     * localdatetime转换为毫秒数
     * @param localDateTime
     * @return long
     */
    @Deprecated
    public static long toLong(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * localdatetime转换为秒数
     * @param localDateTime
     * @return long
     */
    public static long toSecond(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }


    /**
     * 根据出生日期获取年龄
     * @param birthDay
     * @return int
     */
    public static int getAge(LocalDate birthDay) {
        if (birthDay == null) {
            return 0;
        }
        return birthDay.until(LocalDate.now()).getYears();
    }

    /**
     * 根据出生日期获取年龄
     * @param birthDay
     * @return int
     */
    public static int getAge(LocalDateTime birthDay) {
        if (birthDay == null) {
            return 0;
        }
        return birthDay.toLocalDate().until(LocalDate.now()).getYears();
    }


    /**
     * 获取这个月第一天
     * @param localDate
     * @return java.time.LocalDate
     */
    public static LocalDate getFirstDayOfMonth(LocalDate localDate) {
        return localDate.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取这个月最后一天
     * @param localDate
     * @return java.time.LocalDate
     */
    public static LocalDate getLastDayOfMonth(LocalDate localDate) {
        return localDate.with(TemporalAdjusters.lastDayOfMonth());
    }

    @Data
    public static class DateModel {
        private int year;
        private int month;
        private int week;

        private LocalDate startDate;

        private LocalDate endDate;

        public DateModel(int year, int month, int week) {
            this.year = year;
            this.month = month;
            this.week = week;
        }

        public DateModel() {
//            LocalDate now = LocalDate.now();
//            this.year = now.getYear();
//            this.month = now.getMonthValue();
//            this.week = 1;
        }

        public LocalDate getStartDate() {
            return calcDate();
        }

        private LocalDate calcDate() {
            LocalDate localDate = getFirstWeekOfMonth(LocalDate.of(year, month, 1));
            return localDate.plusDays((week - 1) * 7);
        }

        public LocalDate getEndDate() {
            return calcDate().plusDays(6);
        }

        @Override
        public String toString() {
            return this.year + "年" + month + "月第" + week + "周";
        }
    }

    /**
     * 获取这个月的第一周
     * @param localDate
     * @return java.time.LocalDate
     */
    public static LocalDate getFirstWeekOfMonth(LocalDate localDate) {
        LocalDate firstDay = getFirstDayOfMonth(localDate);
        for (int i = 0; i < 6; i++) {
            DayOfWeek day = firstDay.getDayOfWeek();
            if (DayOfWeek.MONDAY.equals(day)) {
                break;
            }
            //往后推一天
            firstDay = firstDay.plusDays(1);
        }
        return firstDay;
    }

    /**
     * 获取这个日期的第一周
     * @param localDate
     * @return java.time.LocalDate
     */
    public static LocalDate getFirstWeekOfDate(LocalDate localDate) {
        // 获得当前日期的所在周的周一(previousOrSame:如果当前日期是周一，就返回当前日期;否则就返回上一个周一。)
        return localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /**
     * 获取这个月第几周数据模型
     * <p>以每月的第一个周一所在的周作为每月第一周</p>
     * @param localDate
     * @return cn.undraw.util.DateUtils.DateModel
     */
    public static DateModel getWeekOfMonth(LocalDate localDate) {
        // 这个月的第一周
        LocalDate firstWeek = getFirstWeekOfMonth(localDate);
        // 这个日期的第一周
        LocalDate localWeek = getFirstWeekOfDate(localDate);
        // 根据两个周一判断是这个月的第几周
        if (firstWeek.isBefore(localWeek) || firstWeek.equals(localWeek)) {
            int year = localWeek.getYear();
            int monthValue = localWeek.getMonthValue();
            int week = (int) ((localWeek.toEpochDay() - firstWeek.toEpochDay()) / 7 + 1);
            return new DateModel(year, monthValue, week);
        } else {
            LocalDate lastDay = getLastDayOfMonth(localDate.minusMonths(1));
            return getWeekOfMonth(lastDay);
        }
    }

    /**
     * 获取这个月是第几周
     * @param localDate
     * @return int
     */
    public static int getWeekValue(LocalDate localDate) {
        return getWeekOfMonth(localDate).getWeek();
    }

    /**
     * 获取这个月有几个周
     * @param localDate
     * @return int
     */
    public static int getWeekTotal(LocalDate localDate) {
        //  这个月的第一周
        LocalDate weekOfMonth = getFirstWeekOfMonth(localDate);
        int count = 0;
        int day = weekOfMonth.getDayOfMonth();
        while (day <= localDate.lengthOfMonth()) {
            if (DayOfWeek.MONDAY.equals(weekOfMonth.getDayOfWeek())) {
                count++;
            }
            day += 7;
        }
        return count;
    }


    /***
     * 获取当月是第几季度
     * @return int
     */
    public static int getQuarter() {
        return getQuarter(LocalDate.now());
    }

    /***
     * 获取这个月是第几季度
     * @param localDate
     * @return int
     */
    public static int getQuarter(LocalDate localDate) {
        return (int)Math.ceil(localDate.getMonthValue() / 3.0);
    }

}