package cn.undraw.util.decimal.annotation;

import cn.undraw.util.decimal.handler.DecimalSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author readpage
 * @date 2022-11-15 10:59
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, TYPE})
@JsonSerialize(using = DecimalSerializer.class)
public @interface DecimalFormat {
    int value() default 2;
    Access access() default DecimalFormat.Access.AUTO;
    public static enum Access {
        AUTO,
        TenThousand;

        private Access() {
        }
    }
}
