package com.example.domain.model;

import lombok.Data;

/**
 * 下拉选项定义
 */
@Data
public class OptionDef {
    private String label;
    private String value;

    public OptionDef() {}
    public OptionDef(String label, String value) {
        this.label = label;
        this.value = value;
    }
}
