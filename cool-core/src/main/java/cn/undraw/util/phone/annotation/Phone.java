package cn.undraw.util.phone.annotation;

import cn.undraw.util.phone.handler.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * @author readpage
 * @date 2022-10-27 14:35
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD})
@Constraint(validatedBy = {PhoneValidator.class})
public @interface Phone {
    String pattern() default "^(13[0-9]|14[014-9]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";

    // 注解的提示信息
    String message() default "手机号码格式不正确";

    Class<?>[] groups() default { }; // groups用来指定分组，可以让校验采取不同的机制，当前默认未指定任何分组机制，默认每次都要进行校验

    Class<? extends Payload>[] payload() default { };

    boolean required() default true;

    // 默认分组
    interface Default{

    }

    // 分组A
    interface A{

    }
}
