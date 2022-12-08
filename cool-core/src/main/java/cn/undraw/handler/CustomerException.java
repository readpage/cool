package cn.undraw.handler;

import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultUtils;

/**
 * @author readpage
 * @date 2022-09-07 14:30
 */
public class CustomerException extends RuntimeException {
    private Result<?> result;

    public CustomerException() {
        result = ResultUtils.fail();
    }

    public CustomerException(Result<?> result) {
        super(result.getMsg());
        this.result = result;
    }

    public CustomerException(String msg) {
        super(msg);
        result = ResultUtils.fail(msg);
    }

    public CustomerException(String msg, Throwable throwable) {
        super(msg, throwable);
        result = ResultUtils.fail(msg, throwable);
    }

    public Result<?> getResult() {
        return result;
    }
}
