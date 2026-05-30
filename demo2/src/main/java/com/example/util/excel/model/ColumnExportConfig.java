package com.example.util.excel.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 列导出配置 — 泛化解耦 FilterParam.ColumnItem
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ColumnExportConfig {

    /** 对齐方式：left/center/right */
    private String align;

    /** 列宽（像素） */
    private Integer width;

    /** 最小列宽（备用） */
    private Integer minWidth;
}
