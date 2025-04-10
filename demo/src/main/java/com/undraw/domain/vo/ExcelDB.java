package com.undraw.domain.vo;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ContentStyle;
import cn.idev.excel.enums.poi.VerticalAlignmentEnum;
import com.undraw.util.excel.converter.UrlConverter;
import com.undraw.util.excel.converter.UrlListConverter;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.List;

/**
 * @author readpage
 * @date 2022-11-22 8:17
 */
//log4j2 lombok的build改成javac
@Log4j2
@Data
@ContentStyle(verticalAlignment = VerticalAlignmentEnum.CENTER)
public class ExcelDB {

    @ExcelProperty(value = "规格")
    private String name;

    @ExcelProperty(value = "图片", converter = UrlConverter.class)
    private String img;

    @ExcelProperty(value = "图片列表", converter = UrlListConverter.class)
    private List<String> imgList;

    @ExcelProperty(value = "介绍")
    private String introduce;

}