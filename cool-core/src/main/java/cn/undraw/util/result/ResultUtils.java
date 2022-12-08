package cn.undraw.util.result;


import cn.undraw.handler.CustomerException;
import cn.undraw.util.ErrorUtils;

import java.util.HashMap;
import java.util.Map;

public class ResultUtils {

    public static<T> Result<T> ok(Integer code, String msg, T data) {
        return new Result<T>(code, msg, data);
    }

    public static Result ok(ResultEnum resultEnum) {
        return ok(resultEnum.getCode(), resultEnum.getMsg(), null);
    }

    public static<T> Result<T> ok(ResultEnum resultEnum, T data) {
        return ok(resultEnum.getCode(), resultEnum.getMsg(), data);
    }

    public static<T> Result<T> ok(String msg, T data) {
        return ok(ResultEnum.SUCCESS.getCode(), msg, data);
    }

    public static<T> Result<T> ok(T data) {
        return ok(ResultEnum.SUCCESS.getMsg(), data);
    }

    public static Result ok(String msg) {
        return ok(msg, null);
    }

    public static Result ok() {
        return ok(ResultEnum.SUCCESS.getMsg());
    }

    public static Result ok(boolean b) {
        return b ? ok() : fail();
    }

    public static Result ok(boolean b, String ok, String fail) {
        return b ? ok(ok) : fail(fail);
    }

    @Deprecated
    public static Result ok(Runnable runnable) {
        runnable.run();
        return ok();
    }

    /**
     * code不等于200，抛出异常
     * @param result
     * @param error 错误信息
     * @return
     * @param <T>
     */
    public static<T> Result<T> status(Result<T> result, String error) {
        if (result.getCode() != 200) {
            throw new CustomerException(error);
        }
        return result;
    }

    public static<T> Result<T> status(Result<T> result) {
        return status(result, ResultEnum.FAIL.getMsg());
    }


    /**
     * 只返回StackTrace的第一个元素
     * @param throwable
     * @return
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



    public static<T> Result<T> fail(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public static<T> Result<T> fail(String msg, T data) {
        return fail(ResultEnum.FAIL.getCode(), msg, data);
    }

    public static Result fail(Integer code, String msg) {
        return fail(code, msg, null);
    }

    public static Result fail(ResultEnum resultEnum) {
        return fail(resultEnum.getCode(), resultEnum.getMsg());
    }

    public static Result fail(String msg) {
        return fail(ResultEnum.FAIL.getCode(), msg);
    }

    public static Result fail() {
        return fail(ResultEnum.FAIL);
    }


    public static<T> Result<T> error(Integer code, String msg, T data) {
        return new Result<>(code, msg, data);
    }

    public static<T> Result<T> error(String msg, T data) {
        return error(ResultEnum.ERROR.getCode(), msg, data);
    }

    public static Result error(String msg) {
        return error(ResultEnum.ERROR.getCode(), msg, null);
    }

    public static Result error() {
        return error(ResultEnum.ERROR.getMsg());
    }



    public static Result fail(String msg, Throwable e) {
        String str = "操作结果:失败(" + e.getMessage() + ")|方法名:" + ErrorUtils.getErrorMethod(e);
        return fail(ResultEnum.FAIL.getCode(), msg, str);
    }

    public static Result fail(Throwable e) {
        return fail(ResultEnum.FAIL.getMsg(), e);
    }

    // List<? super T>表示该集合中存的都是类型T的父类，包括T自己
    public static Result error(String msg, Throwable e) {
        String str = "操作结果:异常(" + e.getMessage() + ")|方法名:" + ErrorUtils.getErrorMethod(e);
        return error(ResultEnum.ERROR.getCode(), msg, str);
    }

    public static Result error(Throwable e) {
        return error(ResultEnum.ERROR.getMsg(), e);
    }

}
