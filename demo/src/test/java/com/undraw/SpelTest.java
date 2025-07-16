package com.undraw;

import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

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
}
