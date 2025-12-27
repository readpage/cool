package com.undraw.core;

import cn.undraw.model.Compare;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DecimalUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.URLUtils;
import cn.undraw.util.bean.BeanUtils;
import cn.undraw.util.bean.SFunction;
import cn.undraw.util.snowflake.SnowflakeUtils;
import com.undraw.domain.entity.User;
import com.undraw.domain.model.Employee;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author readpage
 * @date 2023-03-03 11:20
 */

public class StrTest {

    @Test
    public void test() {
        System.out.println(ConvertUtils.toJson((Object) null));
        List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, null, 3));
        HashSet hashSet = new HashSet();
        hashSet.add(2);
        hashSet.add(5);
        hashSet.add(3);
        System.out.println(StrUtils.join(list, ","));
        System.out.println(StrUtils.join(hashSet, ","));
        System.out.println(StrUtils.join(new String[]{"2", "4", "8"}, "|"));
        String o = "1";
        BigDecimal bigDecimal = null;
        System.out.println(bigDecimal);

        Double a = Double.valueOf(2);
        double b = a;
        System.out.println(b);
        System.out.println(DecimalUtils.toCurrency(null));
    }

    @Test
    public void fillZero() {
        String format = String.format("%06d", 123);
        System.out.println(format);
    }
//
//    @Test
//    public void test2() {
//        String j = String.valueOf("9.318443295E7");
//        String b = String.valueOf("5.959610484E7");
//        System.out.println(DecimalUtils.sub(j, b));
//        double div = DecimalUtils.div(DecimalUtils.sub(j, b), ConvertUtils.toDouble("59596104.840000"));
//        System.out.println(div);
//        System.out.println(DecimalUtils.toPercent(div));
//        double a = 3.45400312412;
//        Double k = 3.454001314;
//        String c = "3.454003124120000";
//        System.out.println((Double)a);
//        System.out.println((double) k);
//        System.out.println(Double.valueOf("3.454003124120000"));
//        System.out.println(new BigDecimal(c).doubleValue());
//    }

//    @Test
//    public void test3() {
//        String j = String.valueOf("9.318443295E7");
//        String b = String.valueOf("5.959610484E7");
//        System.out.println(DecimalUtils.sub(j, b));
//        double div = DecimalUtils.div(DecimalUtils.sub(j, b), ConvertUtils.toDouble("59596104.840000"));
//        System.out.println(div);
//        System.out.println(DecimalUtils.toPercent(div));
//        System.out.println("**************");
//        BigDecimal subtract = new BigDecimal("93184432.950000").subtract(new BigDecimal("59596104.840000"));
//        System.out.println(subtract);
//        System.out.println(subtract.divide(new BigDecimal("59596104.840000"), 10, BigDecimal.ROUND_HALF_UP));
//    }

    @Test
    public void test4() {
        List<Long> numbers = StrUtils.findNumbers("CCN1300050");
        numbers.forEach(System.out::println);
    }

    @Test
    public void removePrefix() {
        String a = "$123";
        String b = "hello world!";
        String c = "hello.txt";
        System.out.println(StrUtils.removePrefix(a, "$"));
        System.out.println(StrUtils.removePrefix(b, "hello "));
        System.out.println(StrUtils.removeSuffix(c, ".txt"));
        String msg = "/driver-trip-record/2023/03-23/捕获3.PNG";
        System.out.println(URLUtils.encode(msg));
    }

    @Test
    public void convert() {
        String string = ConvertUtils.toString(null);
        System.out.println(string);
        System.out.println(string.isEmpty());
        int i = "".indexOf("\n");
        System.out.println(i);
    }

    @Test
    public void test5() {
        String str = "Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '86' for key 'user.PRIMARY'\n" +
                "; Duplicate entry '86' for key 'user.PRIMARY'; ";
        Pattern pattern = Pattern.compile("Duplicate entry '(.+?)' for key");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            String content = matcher.group(1);
            System.out.println("匹配到的内容是: " + content);
        } else {
            System.out.println("没有找到匹配的内容");
        }
    }

    @Test
    public void test6() {
        String str = null;
//        System.out.println(str.equals(null));
        System.out.println(Objects.equals(str, null));
    }

    @Test
    public void test7() {
        String a = "12345678";
        System.out.println(a.substring(Math.max(0, a.length() - 6)));
        String string = ConvertUtils.toString("123");
        System.out.println(string);
        String uuid = UUID.randomUUID().toString();
        System.out.println(uuid.substring(uuid.length() - 8));
        System.out.println(SnowflakeUtils.nextId());
    }

    public <T> void listByKey(SFunction<T, ?>...fun) {
        String fieldName = BeanUtils.getFieldName(fun);
        System.out.println(fieldName);
    }

    @Test
    public void test10() {
        listByKey(User::getUsername, User::getPassword, User::getCreateTime);
    }

    @Test
    public void matcher() {
        String a = "test";
        String b = "t";
        boolean contains = StrUtils.contains(a, b);
        System.out.println(contains);
    }

    @Test
    public void compare() {
        List<Employee> employeeList = Employee.employeeList;
        Employee employee1 = employeeList.get(0);
        Employee employee2 = employeeList.get(1);
        Employee employee3 = employeeList.get(2);
        List<Employee> list1 = new ArrayList<>(Arrays.asList(employee1, employee2));
        List<Employee> list2 = new ArrayList<>(Arrays.asList(employee2, employee3));
        Compare<Employee> compare = StrUtils.compare(list1, list2, (v1, v2) -> Objects.equals(v1.getId(), v2.getId()));
        System.out.println(compare.saveList);
        System.out.println(compare.updateList);
        System.out.println(compare.removeList);
        Compare<Employee> compare1 = StrUtils.compare(list2, new ArrayList<>(), (v1, v2) -> Objects.equals(v1.getId(), v2.getId()));
        System.out.println(compare1);
    }



}
