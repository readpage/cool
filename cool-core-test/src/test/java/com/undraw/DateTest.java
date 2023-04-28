package com.undraw;

import cn.undraw.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

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
        LocalDate localDate = DateUtils.strToDate("2023-02-01");
        System.out.println(DateUtils.getTimeStamp(localDate));
        LocalDate localDate2 = DateUtils.strToDate("2023-03-07");
        System.out.println(DateUtils.getTimeStamp(localDate2));
        log.info("时间戳转localDateTime-->" + DateUtils.stampToDateTime(1669046400000L));
        log.info(DateUtils.stampToDateTime(4261046399000L).toString());
        System.out.println(Instant.ofEpochSecond(1659283200).atZone(ZoneOffset.ofHours(8)).toLocalDateTime());
    }

    @Test
    public void test4() {
        System.out.println(DateUtils.getQuarter(DateUtils.strToDate("2023-12-23")));
    }

}
