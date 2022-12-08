package cn.undraw.util.log.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作日志的操作类型
 * @author readpage
 * @date 2022-11-29 18:47
 */
@Getter
@AllArgsConstructor
public enum OperateTypeEnum {

    /**
     * 错误
     */
    ERROR(-1, "错误"),
    /**
     * 警告
     */
    WARN(-2, "警告"),
    /**
     * 其它
     *
     * 在无法归类时，可以选择使用其它。因为还有操作名可以进一步标识
     */
    OTHER(0, "其它"),
    /**
     * 新增
     */
    CREATE(1, "新增"),
    /**
     * 查询
     */
    READ(2, "查询"),
    /**
     * 修改
     */
    UPDATE(3, "修改"),
    /**
     * 删除
     */
    DELETE(4, "删除"),
    /**
     * 导入
     */
    IMPORT(5, "导入"),
    /**
     * 导出
     */
    EXPORT(6, "导出"),
    /**
     * 上传
     */
    UPLOAD(7, "上传"),
    /**
     * 下载
     */
    DOWNLOAD(8, "下载");

    /**
     * 类型
     */
    private final int type;

    private final String msg;

    public static String getMsg(int type) {
        for (OperateTypeEnum value : OperateTypeEnum.values()) {
            if (value.type == type) {
                return value.msg;
            }
        }
        return OperateTypeEnum.OTHER.getMsg();
    }

}
