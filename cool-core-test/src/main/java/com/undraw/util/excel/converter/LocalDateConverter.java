package com.undraw.util.excel.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author readpage
 * @date 2023-02-08 8:50
 */
public class LocalDateConverter implements Converter<LocalDate> {

    public LocalDateConverter() {
    }

    public Class supportJavaTypeKey() {
        return LocalDate.class;
    }

    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    public LocalDate convertToJavaData(ReadCellData cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws ParseException {
        if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(contentProperty.getDateTimeFormatProperty().getFormat());
            return LocalDate.parse(cellData.getStringValue(), formatter);
        } else {
            return LocalDate.parse(cellData.getStringValue());
        }
    }

    public WriteCellData<String> convertToExcelData(LocalDate value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        DateTimeFormatter formatter;
        if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
            formatter = DateTimeFormatter.ofPattern(contentProperty.getDateTimeFormatProperty().getFormat());
        } else {
            formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        }

        return new WriteCellData(value.format(formatter));
    }
}

