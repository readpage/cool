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
        if(cellData.getType()==CellDataTypeEnum.NUMBER) {
            LocalDate localDate = DateUtils.toLocalDate(DateUtil.getJavaDate(cellData.getNumberValue().doubleValue()));
            return localDate;
        } if (cellData.getType() == CellDataTypeEnum.STRING) {
            return  DateUtils.toLocalDate(cellData.getStringValue());
        }
        return null;
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

