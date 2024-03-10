package com.undraw.util.excel;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.StrUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.undraw.util.excel.converter.LocalDateConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author readpage
 * @date 2023-02-07 13:35
 */
public class ExcelUtils {

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Class<T> clazz) {
        export(response, fileName, dataList, clazz, null);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, dataList, null, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Class<T> clazz, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, "sheet", dataList, clazz, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, sheetName, dataList, null, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class<T> clazz) {
        export(response, fileName, sheetName, dataList, clazz, null);
    }

    /**
     * 导出excel
     * @param response
     * @param fileName
     * @param sheetName
     * @param dataList
     * @param clazz
     * @param c
     * @return void
     */
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

    public static <T> void export(File file, String sheetName, List<T> dataList, Class<T> clazz, Consumer<ExcelWriterSheetBuilder> c) {
        if (file == null) {
            throw new CustomerException("file参数为null");
        }
        try {
            ExcelWriterSheetBuilder writerSheetBuilder;
            writerSheetBuilder = EasyExcel.write(file, clazz).sheet(sheetName);
            writerSheetBuilder.registerConverter(new LocalDateConverter());
            writerSheetBuilder.registerConverter(new LocalDateTimeDateConverter());
            writerSheetBuilder.registerWriteHandler(new AutoHeadColumnWidthStyleStrategy());
            if (StrUtils.isNotEmpty(c)) {
                c.accept(writerSheetBuilder);
            }
            writerSheetBuilder.doWrite(dataList);
        } catch (Throwable t) {
            throw new RuntimeException("Excel文件导出异常");
        }
    }

    /**
     * excel导出多个sheet
     * @param response
     * @param fileName
     * @param excelModelList
     * @param <T>
     */
    public static <T> void moreExport(HttpServletResponse response, String fileName, List<ExcelModel> excelModelList) {
        if (StrUtils.isEmpty(excelModelList)) {
            throw new CustomerException("List<ExcelModel> excelModelList IS NULL");
        }
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            for (int i = 0; i < excelModelList.size(); i++) {
                ExcelModel excelModel = excelModelList.get(i);
                ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet(i, excelModel.getSheetName()).head(excelModel.getClazz());
                writerSheetBuilder.registerConverter(new LocalDateConverter());
                writerSheetBuilder.registerConverter(new LocalDateTimeDateConverter());
                writerSheetBuilder.registerWriteHandler(new AutoHeadColumnWidthStyleStrategy());
                Consumer<ExcelWriterSheetBuilder> c = excelModel.getC();
                if (StrUtils.isNotEmpty(c)) {
                    c.accept(writerSheetBuilder);
                }
                WriteSheet writeSheet = writerSheetBuilder.build();
                excelWriter.write(excelModel.getDataList(), writeSheet);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void moreExport(File file, List<ExcelModel> excelModelList) {
        if (file == null) {
            throw new CustomerException("file参数为null");
        }
        if (StrUtils.isEmpty(excelModelList)) {
            throw new CustomerException("List<ExcelModel> excelModelList IS NULL");
        }
        try (ExcelWriter excelWriter = EasyExcel.write(file).build()) {
            for (int i = 0; i < excelModelList.size(); i++) {
                ExcelModel excelModel = excelModelList.get(i);
                ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet(i, excelModel.getSheetName()).head(excelModel.getClazz());
                writerSheetBuilder.registerConverter(new LocalDateConverter());
                writerSheetBuilder.registerConverter(new LocalDateTimeDateConverter());
                writerSheetBuilder.registerWriteHandler(new AutoHeadColumnWidthStyleStrategy());
                Consumer<ExcelWriterSheetBuilder> c = excelModel.getC();
                if (StrUtils.isNotEmpty(c)) {
                    c.accept(writerSheetBuilder);
                }
                WriteSheet writeSheet = writerSheetBuilder.build();
                excelWriter.write(excelModel.getDataList(), writeSheet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExcelModel {
        private String sheetName;
        private Class clazz;
        private List dataList;
        private Consumer<ExcelWriterSheetBuilder> c;

        public ExcelModel(String sheetName, Class clazz, List dataList) {
            this.sheetName = sheetName;
            this.clazz = clazz;
            this.dataList = dataList;
        }
    }

}

