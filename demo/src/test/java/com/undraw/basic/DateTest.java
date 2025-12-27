package com.undraw.basic;

import cn.undraw.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author readpage
 * @date 2023-01-17 16:48
 */
@Slf4j
public class DateTest {
    @Test
    public void test() {
        System.out.println(DateUtils.now());
    }

    @Test
    public void test2() {
        LocalDate now = LocalDate.now();
        System.out.println(DateUtils.getFirstDayOfMonth(now));
        System.out.println(DateUtils.getLastDayOfMonth(now));
        System.out.println("--------以每月的第一个周一所在的周作为每月第一周----------------");
        System.out.println(DateUtils.getWeekOfMonth(now));
        System.out.println(DateUtils.getWeekOfMonth(now.withDayOfMonth(3)));
        System.out.println("------计算一个月有几周");
        LocalDate parse = LocalDate.parse("2023-02-28");
        System.out.println("有" + DateUtils.getWeekTotal(parse) + "周");
        DateUtils.DateModel dateModel = DateUtils.getWeekOfMonth(parse);
        System.out.println("----------------------");
        System.out.println(dateModel.getStartDate() + "---" + dateModel.getEndDate());
        LocalDate startDate = DateUtils.getFirstDayOfMonth(now.minusMonths(1));
        LocalDate endDate = DateUtils.getFirstDayOfMonth(now);
        System.out.println(startDate + "->" + endDate);
    }

    @Test
    public void test3() {
        LocalDateTime startTime = DateUtils.toDateTime("2024-11-01 00:00:00");
        System.out.println(DateUtils.toMilli(startTime));
        LocalDateTime endTime = DateUtils.toDateTime("2024-11-30 00:00:00");
        System.out.println(DateUtils.toMilli(endTime));

        LocalDate localDate2 = DateUtils.toLocalDate("2023-02-01");
        System.out.println(DateUtils.toLong(localDate2));
        log.info("时间戳转localDateTime-->" + DateUtils.toDateTime(1669046400000L).toString());
        log.info(DateUtils.toDateTime(4261046399000L).toString());
        System.out.println(Instant.ofEpochSecond(1659283200).atZone(ZoneOffset.ofHours(8)).toLocalDateTime());
    }

    @Test
    public void test4() {
        LocalDateTime localDateTime = DateUtils.toDateTime("2023-05-05 12:32:25");
        LocalDateTime localDateTime1 = DateUtils.toDateTime("2023-05-05 12:32:25");
        Date date = DateUtils.toDate(localDateTime);
        System.out.println(localDateTime1.equals(DateUtils.toDateTime(date)));
        System.out.println(DateUtils.getQuarter(DateUtils.toLocalDate("2023-12-23")));
    }


    @Test
    public void test5() {
        LocalDate localDate = DateUtils.toLocalDate(new Date());
        System.out.println(localDate);
    }

    @Test
    public void between() {
        LocalDate startDate = LocalDate.parse("2025-02-01");
        LocalDate endDate = LocalDate.parse("2025-04-28");
        long between = ChronoUnit.DAYS.between(startDate, endDate);
        System.out.println(between);
        long between2 = ChronoUnit.MONTHS.between(startDate, endDate);
        System.out.println(between2);
    }

    /**
     * unix 时间戳
     */
    @Test
    public void second() {
        LocalDateTime startTime = DateUtils.toDateTime("2025-03-26 00:00:00");
        LocalDateTime endTime = DateUtils.toDateTime("2025-03-27 00:00:00");
        System.out.println(DateUtils.toMilli(startTime));
        System.out.println(DateUtils.toMilli(endTime));


        System.out.println(DateUtils.toDateTime(1609430400000L));
    }

    @Test
    public void after() {
        LocalDate start = LocalDate.parse("2024-01-01");
        LocalDate end = LocalDate.parse("2024-03-01");
        System.out.println(start.isBefore(start));
        System.out.println(start.isAfter(end));
    }

    @Test
    public void range() {
        LocalDate now = LocalDate.now();
        LocalTime localTime = LocalTime.now();
        System.out.println(localTime.withSecond(0).withNano(0) + "--" + localTime.withSecond(59).withNano(999_999_999));
        System.out.println(now.atTime(LocalTime.MIN) + "--" + now.atTime(LocalTime.MAX));
    }

    @Test
    public void before() {
        System.out.println(deadline());
    }

    public boolean deadline() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 创建16:00的时间点
        LocalTime timeOfDay = LocalTime.of(16, 0); // 16:00
        return !now.toLocalTime().isBefore(timeOfDay);
    }

}
