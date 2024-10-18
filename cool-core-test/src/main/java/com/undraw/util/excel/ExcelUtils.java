package com.undraw.util.excel;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.StrUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.undraw.util.excel.converter.LocalDateConverter;
import com.undraw.util.excel.converter.LocalDateTimeConverter;
import com.undraw.util.excel.handler.ImageCellWriteHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author readpage
 * @date 2023-02-07 13:35
 */
public class ExcelUtils {

    public static <T> List<T> read(InputStream inputStream, Class clazz, int headRowIndex) {
        List<T> list = new ArrayList<>();
        EasyExcel.read(inputStream)
                .head(clazz)
                .registerConverter(new LocalDateConverter())
                .registerConverter(new LocalDateTimeConverter())
                .sheet()
                .registerReadListener(new PageReadListener<T>(dataList -> {
                    list.addAll(dataList);
                    System.out.println(dataList);
                }))
                .headRowNumber(headRowIndex).doRead();

        return list;
    }

    public static <T> List<T> read(MultipartFile file, Class<T> clazz) {
        try {
            return read(file.getInputStream(), clazz, 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // --> fill
    public static <T> void fill(ExcelWriterBuilder excelWriterBuilder, List<T> list, Consumer<ExcelWriterSheetBuilder> c) {
        try (ExcelWriter excelWriter = excelWriterBuilder.build()) {
            ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet();
            if (StrUtils.isNotEmpty(c)) {
                c.accept(writerSheetBuilder);
            }
            WriteSheet writeSheet = writerSheetBuilder.build();
            excelWriter.fill(list, writeSheet);
        }
    }

    public static <T> void fill(File file, File templateFile, List<T> list, Consumer<ExcelWriterSheetBuilder> c) {
        ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(file).withTemplate(templateFile);
        fill(excelWriterBuilder, list, c);
    }

    public static <T> void fill(File file, File templateFile, List<T> list) {
        fill(file, templateFile, list, null);
    }

    public static <T> void fill(HttpServletResponse response, String fileName, File templateFile, List<T> list, Consumer<ExcelWriterSheetBuilder> c) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        try {
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream()).withTemplate(templateFile);
            fill(excelWriterBuilder, list, c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void fill(HttpServletResponse response, String filename, File templateFile, List<T> list) {
        fill(response, filename, templateFile, list, null);
    }

    // <--

    public static <T> void export(ExcelWriterBuilder excelWriterBuilder, List<ExcelModel> excelModelList) {
        if (StrUtils.isEmpty(excelModelList)) {
            throw new CustomerException("List<ExcelModel> excelModelList IS NULL");
        }
        try (ExcelWriter excelWriter = excelWriterBuilder.build()) {
            for (int i = 0; i < excelModelList.size(); i++) {
                ExcelModel excelModel = excelModelList.get(i);
                ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet(i, excelModel.getSheetName());
                if (excelModel.getHead() != null) {
                    writerSheetBuilder.head(excelModel.getHead());
                } else {
                    writerSheetBuilder.head(excelModel.getClazz());
                }
                writerSheetBuilder.registerConverter(new LocalDateConverter());
                writerSheetBuilder.registerConverter(new LocalDateTimeConverter());
                writerSheetBuilder.registerWriteHandler(new AutoHeadColumnWidthStyleStrategy());
                writerSheetBuilder.registerWriteHandler(new ImageCellWriteHandler());
                Consumer<ExcelWriterSheetBuilder> c = excelModel.getC();
                if (StrUtils.isNotEmpty(c)) {
                    c.accept(writerSheetBuilder);
                }
                excelWriter.writeContext().writeWorkbookHolder().getWorkbook().setForceFormulaRecalculation(true);
                WriteSheet writeSheet = writerSheetBuilder.build();
                excelWriter.write(excelModel.getDataList(), writeSheet);
            }
        }
    }

    /**
     * excel导出多个sheet
     * @param file
     * @param excelModelList
     * @param <T>
     */
    public static <T> void moreExport(File file, List<ExcelModel> excelModelList) {
        if (file == null) {
            throw new CustomerException("file参数为null");
        }
        export(EasyExcel.write(file), excelModelList);
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
        try {
            export(EasyExcel.write(response.getOutputStream()), excelModelList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 导出excel
     * @param file
     * @param sheetName
     * @param dataList
     * @param clazz
     * @param head
     * @param c
     * @param <T>
     */
    public static <T> void export(File file, String sheetName, List<T> dataList, Class clazz, List<List<String>> head, Consumer<ExcelWriterSheetBuilder> c) {
        ExcelModel excelModel = new ExcelModel(sheetName, dataList, clazz, head, c);
        moreExport(file, Arrays.asList(excelModel));
    }

    public static <T> void export(File file, String sheetName, List<T> dataList, List<List<String>> head, Consumer<ExcelWriterSheetBuilder> c) {
        export(file, sheetName, dataList, null, head, c);
    }

    public static <T> void export(File file, String sheetName, List<T> dataList, List<List<String>> head) {
        export(file, sheetName, dataList, head, null);
    }




    public static <T> void export(File file, String sheetName, List<T> dataList, Class clazz, Consumer<ExcelWriterSheetBuilder> c) {
        export(file, sheetName, dataList, clazz, null, c);
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
    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class clazz, List<List<String>> head, Consumer<ExcelWriterSheetBuilder> c) {
        ExcelModel excelModel = new ExcelModel(sheetName, dataList, clazz, head, c);
        moreExport(response, fileName, Arrays.asList(excelModel));
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, List<List<String>> head, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, sheetName, dataList, null, head, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, List<List<String>> head) {
        export(response, fileName, sheetName, dataList, head, null);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class clazz, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, sheetName, dataList, clazz, null, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Class clazz, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, "sheet", dataList, clazz, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, sheetName, dataList, (Class)null, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Consumer<ExcelWriterSheetBuilder> c) {
        export(response, fileName, dataList, null, c);
    }

    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList, Class clazz) {
        export(response, fileName, sheetName, dataList, clazz, null);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList, Class clazz) {
        export(response, fileName, null, dataList, clazz);
    }



    public static <T> void export(HttpServletResponse response, String fileName, String sheetName, List<T> dataList) {
        export(response, fileName, sheetName, dataList, (Class)null);
    }

    public static <T> void export(HttpServletResponse response, String fileName, List<T> dataList) {
        export(response, fileName, dataList, null, null);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExcelModel {
        private String sheetName;

        private List dataList;

        private Class clazz;

        private List<List<String>> head;

        private Consumer<ExcelWriterSheetBuilder> c;

        public ExcelModel(String sheetName, List dataList, Class clazz, Consumer<ExcelWriterSheetBuilder> c) {
            this.sheetName = sheetName;
            this.dataList = dataList;
            this.clazz = clazz;
            this.c = c;
        }

        public ExcelModel(String sheetName, List dataList, List<List<String>> head) {
            this.sheetName = sheetName;
            this.dataList = dataList;
            this.head = head;
        }

        public ExcelModel(String sheetName, List dataList, Consumer<ExcelWriterSheetBuilder> c) {
            this.sheetName = sheetName;
            this.dataList = dataList;
            this.c = c;
        }

        public ExcelModel(String sheetName, List dataList, Class clazz) {
            this.sheetName = sheetName;
            this.dataList = dataList;
            this.clazz = clazz;
        }

        public ExcelModel(String sheetName, List dataList) {
            this.sheetName = sheetName;
            this.dataList = dataList;
        }

        public List getDataList() {
            this.dataList = StrUtils.isNull(ConvertUtils.cloneDeep(dataList), new ArrayList());
            return dataList;
        }

        public Class getClazz() {
            if (clazz == null && StrUtils.isNotEmpty(dataList) && dataList.size() >= 1) {
                Object firstElement = dataList.iterator().next();
                this.clazz = (Class<T>) firstElement.getClass();
            }
            return clazz;
        }
    }

}


