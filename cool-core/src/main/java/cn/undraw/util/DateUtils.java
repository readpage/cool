package cn.undraw.util;

import lombok.Data;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
/**
 * @author readpage
 * @date 2022-12-01 17:27
 */
public class DateUtils {
    // 时区
//    private static String zoneId = "Asia/Shanghai";
    // 日期格式
    private static String dateFormat = "yyyy-MM-dd";

    private static String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    /**
     * 传日期时间字符串返回日期时间
     * @param dateTime
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime toDateTime(String dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
        return LocalDateTime.parse(dateTime, dateTimeFormatter);
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
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimeFormat);
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
     * @param dateTime
     * @return java.time.LocalDate
     */
    public static LocalDate toLocalDate(String dateTime) {
        if (dateTime == null) return null;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDate localDate= LocalDate.parse(dateTime, dateTimeFormatter);
        return localDate;
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
     * date转换为localDate
     * @param date
     * @return java.time.LocalDate
     */
    public static LocalDate toLocalDate(Date date) {
        return toDateTime(date).toLocalDate();
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
    public static long toLong()  {
        return toLong(LocalDateTime.now());
    }

    /**
     * localDate转毫秒数
     * @param localDate
     * @return long
     */
    public static long toLong(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        return localDate.atStartOfDay(zoneId).toInstant().toEpochMilli();
    }

    /**
     * localdatetime转换为毫秒数
     * @param localDateTime
     * @return long
     */
    public static long toLong(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
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