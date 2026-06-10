package com.example.util.excel.converter;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.text.SimpleDateFormat;

/**
 * java.sql.Date 转换器
 */
public class SqlDateConverter implements Converter<java.sql.Date> {

    private static final String FORMAT = "yyyy-MM-dd";

    @Override
    public Class<?> supportJavaTypeKey() {
        return java.sql.Date.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(java.sql.Date value,
            ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT);
        return new WriteCellData<>(sdf.format(value));
    }
}
