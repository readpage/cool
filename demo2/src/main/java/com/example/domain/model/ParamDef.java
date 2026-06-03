package com.example.domain.model;

import lombok.Data;
import java.util.List;

/**
 * 报告查询参数定义
 */
@Data
public class ParamDef {
    /** 参数名（模板中使用 #{name}） */
    private String name;
    /** 显示标签 */
    private String label;
    /** 参数类型：text / number / date / daterange / select / remote-select */
    private String type;
    /** 默认值 */
    private String defaultValue;
    /** 是否必填 */
    private Boolean required;
    /** type=select 时的静态选项 */
    private List<OptionDef> options;
    /** type=remote-select 时的远程选项类型标识 */
    private String optionType;
}
