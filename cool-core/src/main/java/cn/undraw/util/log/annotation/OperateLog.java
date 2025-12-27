package cn.undraw.util.log.annotation;

import cn.undraw.util.log.enums.OperateTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 操作日志注解
 * @author readpage
 * @date 2022-11-29 18:55
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {

    /**
     * 操作模块 <p>为空时，会尝试读取 {@link Tag#name()} 属性</p>
     * @return java.lang.String
     */
    String module() default "";

    /**
     * 操作名 <P>为空时，会尝试读取 {@link Operation#summary()} 属性</P>
     * @return java.lang.String
     */
    String name() default "";

    /**
     * 操作分类 <p>实际并不是数组，因为枚举不能设置 null 作为默认值</p>
     * @return cn.undraw.util.log.enums.OperateTypeEnum
     */
    OperateTypeEnum type() default OperateTypeEnum.OTHER;
}
