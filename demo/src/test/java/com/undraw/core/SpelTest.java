package com.undraw.core;

import cn.undraw.util.DecimalUtils;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.util.Map;

public class SpelTest {

    @Test
    public void spel() {
        SpelExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("a", 1);
        context.setVariable("b", 2);
        context.setVariable("c", 3);
        Integer value = parser.parseExpression("(#a+#b)*#c").getValue(context, Integer.class);
        System.out.println(value);
    }

    @Test
    public void spel2() {
        BigDecimal v = DecimalUtils.spel("#a*#b", Map.of("a", 152431.0, "b", 0.045), BigDecimal.class);
        System.out.println(v);
        BigDecimal v2 = DecimalUtils.spel("#a*#b", Map.of("a", new BigDecimal("152431.0"), "b", new BigDecimal("0.045")), BigDecimal.class);
        System.out.println(v2);
        BigDecimal v3 = DecimalUtils.calc("#a*#b", Map.of("a", 152431.0, "b", 0.045));
        System.out.println(v3);

        String score = DecimalUtils.spel("(#score >= 90) ? '优秀' : (#score >= 60) ? '合格' : '不合格'", Map.of("score", 60), String.class);
        System.out.println(score);
    }

    @Test
    public void tmp() {
        Double a = 152431.0;
        Double b = 0.045;
        Double v = a * b;
        System.out.println(v);

        BigDecimal v2 = BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b));
        System.out.println(v2);

        SpelExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("a", BigDecimal.valueOf(a));
        context.setVariable("b", BigDecimal.valueOf(b));
        Double value = parser.parseExpression("#a.multiply(#b)").getValue(context, Double.class);
        System.out.println(value);

        BigDecimal value2 = parser.parseExpression("152431.0*0.045").getValue(BigDecimal.class);
        System.out.println(value2);
    }

    @Test
    public void test() {
        BigDecimal b = new BigDecimal("1");
        BigDecimal c = new BigDecimal("2");
        BigDecimal d = new BigDecimal("3");
        System.out.println(b.subtract(c).multiply(d));
    }
}
