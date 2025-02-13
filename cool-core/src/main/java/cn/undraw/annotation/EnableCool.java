package cn.undraw.annotation;

import cn.undraw.context.ComponentScanModule;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @author readpage
 * @date 2022-12-08 9:01
 */
@Target(TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Import({ComponentScanModule.class})
public @interface EnableCool {
    String type() default "";

}
