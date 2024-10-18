package cn.undraw.util.result;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.ErrorUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-10-27 14:39
 */
@Tag(name = "R", description = "返回信息")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(title = "状态码")
    private Integer code;
    
    @Schema(title = "返回消息")
    private String msg;
    
    @Schema(title = "承载数据")
    private T data;


    public static<T> R<T> ok(Integer code, String msg, T data) {
        return new R<T>(code, msg, data);
    }

    public static R ok(ResultEnum ResultEnum) {
        return ok(ResultEnum.getCode(), ResultEnum.getMsg(), null);
    }

    public static<T> R<T> ok(ResultEnum ResultEnum, T data) {
        return ok(ResultEnum.getCode(), ResultEnum.getMsg(), data);
    }

    public static<T> R<T> ok(String msg, T data) {
        return ok(ResultEnum.SUCCESS.getCode(), msg, data);
    }

    public static<T> R<T> ok(T data) {
        return ok(ResultEnum.SUCCESS.getMsg(), data);
    }

    public static R ok(String msg) {
        return ok(msg, null);
    }

    public static R ok() {
        return ok(ResultEnum.SUCCESS.getMsg());
    }

    public static R ok(boolean b) {
        return b ? ok() : fail();
    }

    public static R ok(boolean b, String ok, String fail) {
        return b ? ok(ok) : fail(fail);
    }

    public static R ok(Runnable runnable) {
        runnable.run();
        return ok();
    }

    /**
     * code不等于200，抛出异常
     * @param r
     * @param error
     * @return cn.undraw.util.result.R<T>
     */
    public static<T> R<T> status(R<T> r, String error) {
        if (r.getCode() != 200) {
            throw new CustomerException(error);
        }
        return r;
    }

    public static<T> R<T> status(R<T> r) {
        return status(r, ResultEnum.FAIL.getMsg());
    }


    /**
     * 只返回StackTrace的第一个元素
     * @param throwable
     * @return java.util.Map
     */
    @Deprecated
    private static Map getStackTrace(Throwable throwable) {
        Object data = null;
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            data = stackTrace[0];
        }
        HashMap<Object, Object> map = new HashMap<>();
        map.put("stackTrace", data);
        return map;
    }



    public static<T> R<T> fail(Integer code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public static<T> R<T> fail(String msg, T data) {
        return fail(ResultEnum.FAIL.getCode(), msg, data);
    }

    public static R fail(Integer code, String msg) {
        return fail(code, msg, null);
    }

    public static R fail(ResultEnum ResultEnum) {
        return fail(ResultEnum.getCode(), ResultEnum.getMsg());
    }

    public static R fail(String msg) {
        return fail(ResultEnum.FAIL.getCode(), msg);
    }

    public static R fail() {
        return fail(ResultEnum.FAIL);
    }

    public static R fail(Throwable e) {
        String msg = String.format("%s(%s)", ResultEnum.ERROR.getMsg(), e.getMessage());
        return fail(msg, ErrorUtils.getStackTrace(e));
    }

    public static R fail(String msg, Throwable e) {
        return fail(ResultEnum.FAIL.getCode(), msg, ErrorUtils.getStackTrace(e));
    }

    public static R fail(ResultEnum resultEnum, String msg) {
        return fail(resultEnum.getCode(), msg);
    }


    public static R bad(String msg) {
        return fail(ResultEnum.BAD_REQUEST.getCode(), msg);
    }



    public static<T> R<T> error(Integer code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public static<T> R<T> error(String msg, T data) {
        return error(ResultEnum.ERROR.getCode(), msg, data);
    }

    public static R error(String msg) {
        return error(ResultEnum.ERROR.getCode(), msg, null);
    }

    public static R error() {
        return error(ResultEnum.ERROR.getMsg());
    }

    public static R error(String msg, Throwable e) {
        return error(ResultEnum.ERROR.getCode(), msg, ErrorUtils.getStackTrace(e));
    }

    public static R error(Throwable e) {
        String msg = String.format("%s(%s)", ResultEnum.ERROR.getMsg(), e.getMessage());
        return error(msg, e);
    }

    public static R error(ResultEnum resultEnum, Throwable e) {
        return error(resultEnum.getCode(), resultEnum.getMsg(), ErrorUtils.getStackTrace(e));
    }



}
