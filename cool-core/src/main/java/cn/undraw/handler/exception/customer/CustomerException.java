package cn.undraw.handler.exception.customer;


import cn.undraw.util.result.R;

/**
 * @author readpage
 * @date 2022-09-07 14:30
 */
public class CustomerException extends RuntimeException {
    private R<?> R;

    public CustomerException() {
        R = R.fail();
    }

    public CustomerException(R<?> R) {
        super(R.getMsg());
        this.R = R;
    }

    public CustomerException(String msg) {
        super(msg);
        R = R.fail(msg);
    }

    public CustomerException(String msg, Throwable throwable) {
        super(msg, throwable);
        R = R.fail(msg, throwable);
    }

    public R<?> getR() {
        return R;
    }
}
