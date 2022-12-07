package cn.undraw.util.result;

public enum ResultEnum {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "错误的请求"),
    FAIL(401, "操作失败"),
    USER_NOT_LOGIN(402, "用户未登录"),
    NO_PERMISSION(403, "没有操作权限"),
    NOT_FOUND(404, "路径不存在，请检查路径是否正确"),
    ACCOUNT_NOT_EXIST(405, "账号不存在"),
    PASSWORD_ERROR(406, "密码错误"),
    TOKEN_IS_INVALID(407, "无效或过期token"),
    ACCOUNT_DISABLE(408, "账户已禁用"),
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
}
