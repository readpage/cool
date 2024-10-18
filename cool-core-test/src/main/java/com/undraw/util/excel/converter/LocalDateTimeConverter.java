package com.undraw.util.excel.converter;


import cn.undraw.util.DateUtils;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
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
