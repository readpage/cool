package com.undraw.util.excel;

import cn.undraw.util.StrUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.undraw.util.excel.converter.LocalDateConverter;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author readpage
 * @date 2023-02-07 13:35
 */
public class ExcelUtils {

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList) {
        export(response, fileName, sheetName, dataList, null, null);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, sheetName, dataList, null, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class<T> clazz) {
        export(response, fileName, sheetName, dataList, clazz, null);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Class<T> clazz, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, "sheet", dataList, clazz, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class<T> clazz, Consumer<ExcelWriterSheetBuilder> c) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            ExcelWriterSheetBuilder writerSheetBuilder;
            writerSheetBuilder = EasyExcel.write(response.getOutputStream(), clazz).sheet(sheetName);
            writerSheetBuilder.registerConverter(new LocalDateConverter());
            writerSheetBuilder.registerConverter(new LocalDateTimeDateConverter());
            writerSheetBuilder.registerWriteHandler(new AutoHeadColumnWidthStyleStrategy());
            if (StrUtils.isNotEmpty(c)) {
                c.accept(writerSheetBuilder);
            }
            writerSheetBuilder.doWrite(dataList);
        } catch (Throwable t) {
            throw new RuntimeException();
        }
    }

}

