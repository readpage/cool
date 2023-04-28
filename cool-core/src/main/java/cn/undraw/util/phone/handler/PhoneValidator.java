package cn.undraw.util.phone.handler;

import cn.undraw.util.StrUtils;
import cn.undraw.util.phone.annotation.Phone;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author readpage
 * @date 2022-10-27 14:39
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {
    // 注解对象
    private Phone phone;

    @Override
    public void initialize(Phone constraintAnnotation) {
       phone = constraintAnnotation;
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (StrUtils.isEmpty(s)) {
            return false;
        }
        // 获取【Phone】对象的手机格式验证表达式
        String pattern = phone.pattern();
        Pattern compile = Pattern.compile(pattern);
        Matcher matcher = compile.matcher(s);
        return matcher.matches();
    }
}
