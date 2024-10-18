package com.undraw.util.excel.converter;

import cn.undraw.util.StrUtils;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.IoUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class UrlListConverter implements Converter<List<String>> {

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(List<String> value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        List<ImageData> imageDataList = new ArrayList<>();
        for (String url : value) {
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
