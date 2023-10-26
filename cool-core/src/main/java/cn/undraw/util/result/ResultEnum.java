package cn.undraw.util.result;

/**
 * @author readpage
 * @date 2022-10-27 14:39
 */
public enum ResultEnum {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "错误的请求"),
    NO_AUTHORIZATION(401, "未授权"),
    USER_NOT_LOGIN(402, "用户未登录"),
    NO_PERMISSION(403, "没有操作权限"),
    NOT_FOUND(404, "路径不存在，请检查路径是否正确"),
    METHOD_NOT_ALLOWED(405, "请求方式不允许"),
    ACCOUNT_NOT_EXIST(406, "账号不存在"),
    PASSWORD_ERROR(407, "密码错误"),
    TOKEN_IS_EXPIRE(408, "过期token"),
    TOKEN_IS_INVALID(409, "无效token"),
    ACCOUNT_DISABLE(410, "账户已禁用"),
    FAIL(510, "操作失败"),
    TIMEOUT(504, "连接超时"),
    ERROR(500, "系统异常"),
    ;

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public R getR() {
        return R.ok(this);
    }

    public R getR(Throwable e) {
        return R.error(this, e);
    }
}
