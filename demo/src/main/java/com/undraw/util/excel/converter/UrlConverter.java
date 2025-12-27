package com.undraw.util.excel.converter;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ImageData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.idev.excel.util.IoUtils;
import cn.undraw.util.StrUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UrlConverter implements Converter<String> {
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        List<ImageData> imageDataList = new ArrayList<>();
        List<String> urlList = StrUtils.toList(value, ",");
        for (String url : urlList) {
            try {
                if (StrUtils.isNotEmpty(url)) {
                    URL imageUrl = new URL(url);
                    byte[] bytes = IoUtils.toByteArray(imageUrl.openConnection().getInputStream());
                    ImageData imageData = new ImageData();
                    imageData.setImage(bytes);
                    imageDataList.add(imageData);
                }
            } catch (Exception e) {
                log.warn("图片地址: " + url + " 不是有效的!", e);
            }
        }
        WriteCellData writeCellData = new WriteCellData();
        writeCellData.setImageDataList(imageDataList);
        writeCellData.setType(CellDataTypeEnum.EMPTY);
        return writeCellData;
    }
}
