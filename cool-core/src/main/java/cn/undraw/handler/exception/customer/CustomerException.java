package cn.undraw.handler.exception.customer;


import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;

/**
 * @author readpage
 * @date 2022-09-07 14:30
 */
public class CustomerException extends RuntimeException {
    private R r;

    public CustomerException() {
        r = R.fail();
    }

    public CustomerException(R<?> r) {
        super(r.getMsg());
        this.r = r;
    }

    public CustomerException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.r = R.fail(resultEnum);
    }

    public CustomerException(String msg) {
        super(msg);
        r = r.fail(msg);
    }

    public CustomerException(String msg, Throwable throwable) {
        super(msg, throwable);
        r = r.fail(msg, throwable);
    }

    public R<?> getR() {
        return r;
    }
}
