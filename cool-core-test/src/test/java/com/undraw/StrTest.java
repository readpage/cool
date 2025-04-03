package com.undraw;

import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DecimalUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.URLUtils;
import cn.undraw.util.snowflake.SnowflakeUtils;
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

        Double a = new Double(2);
        double b = a;
        System.out.println(b);
        System.out.println(DecimalUtils.toCurrency(null));
    }

    @Test
    public void test2() {
        String j = String.valueOf("9.318443295E7");
        String b = String.valueOf("5.959610484E7");
        System.out.println(DecimalUtils.sub(j, b));
        double div = DecimalUtils.div(DecimalUtils.sub(j, b), ConvertUtils.toDouble("59596104.840000"));
        System.out.println(div);
        System.out.println(DecimalUtils.toPercent(div));
        double a = 3.45400312412;
        Double k = 3.454001314;
        String c = "3.454003124120000";
        System.out.println((Double)a);
        System.out.println((double) k);
        System.out.println(Double.valueOf("3.454003124120000"));
        System.out.println(new BigDecimal(c).doubleValue());
    }

    @Test
    public void test3() {
        String j = String.valueOf("9.318443295E7");
        String b = String.valueOf("5.959610484E7");
        System.out.println(DecimalUtils.sub(j, b));
        double div = DecimalUtils.div(DecimalUtils.sub(j, b), ConvertUtils.toDouble("59596104.840000"));
        System.out.println(div);
        System.out.println(DecimalUtils.toPercent(div));
        System.out.println("**************");
        BigDecimal subtract = new BigDecimal("93184432.950000").subtract(new BigDecimal("59596104.840000"));
        System.out.println(subtract);
        System.out.println(subtract.divide(new BigDecimal("59596104.840000"), 10, BigDecimal.ROUND_HALF_UP));
    }

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

    @Test
    public void test8() {
        String v = "s0084";
        String str = "{\n" +
                "  \"corpAccessToken\": \"53D3623AB81A17271413EBB54CC344EF\",\n" +
                "  \"corpId\": \"FSCID_245BECA0559FADFA95330EBAF6C802E0\",\n" +
                "  \"currentOpenUserId\": \"FSUID_217199FEE84B0E5D719D6521B7B22BB1\",\n" +
                "  \"data\": {\n" +
                "    \"search_query_info\": {\n" +
                "      \"offset\": 0,\n" +
                "      \"limit\": 10,\n" +
                "      \"filters\": [\n" +
                "        {\n" +
                "          \"field_name\": \"employee_number\",\n" +
                "          \"field_values\": [\n" +
                "            \"%s\"\n" +
                "          ],\n" +
                "          \"operator\": \"EQ\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    \"dataObjectApiName\": \"PersonnelObj\"\n" +
                "  }\n" +
                "}";
        str = String.format(str, "s0084");
        Map s = ConvertUtils.toMap(str);
        System.out.println(s);
    }

    @Test
    public void test9() {
        String category = "COSUS0000009";
        List<Long> numbers = StrUtils.findNumbers(category);
        String format = String.format("%s%06d", category.replaceAll("\\d+", ""), numbers.get(0) + 1L);
        System.out.println(format);

        Integer a = new Integer(1);
        Integer b = new Integer(1);

        System.out.println( a == b);
        System.out.println(Objects.equals(a, b));
    }

    @Test
    public void test10() {
        String test = getSort("test");
        System.out.println(test);
    }

    @Test
    public void matcher() {
        String a = "test";
        String b = "t";
        boolean contains = StrUtils.contains(a, b);
        System.out.println(contains);
    }

    public String getSort(String sort) {
        try {
            ConvertUtils.cloneDeep(sort, Map.class);
            return sort;
        } catch (Exception e) {
            return null;
        }
    }

}
