package com.undraw.util.excel.converter;


import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.undraw.util.DateUtils;
import org.apache.poi.ss.usermodel.DateUtil;

import java.text.ParseException;
import java.time.LocalDateTime;

public class LocalDateTimeConverter implements Converter<LocalDateTime> {
    public LocalDateTimeConverter() {
    }

    public Class supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    public LocalDateTime convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws ParseException {
        if(cellData.getType() == CellDataTypeEnum.NUMBER) {
            LocalDateTime localDateTime = DateUtils.toDateTime(DateUtil.getJavaDate(cellData.getNumberValue().doubleValue()));
            return localDateTime;
        } if (cellData.getType() == CellDataTypeEnum.STRING) {
            return DateUtils.toDateTime(cellData.getStringValue());
        }
        return null;
    }

    public WriteCellData<String> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return new WriteCellData(DateUtils.toString(value));
    }
}
