package com.undraw;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;

import java.util.ArrayList;
import java.util.List;

public class EasyExcelExample {

    public static void main(String[] args) {
        String fileName = "example.xlsx";
 
        // 创建写入工作簿
        EasyExcel.write(fileName, ExcelData.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy()) // 设置自适应列宽
                .sheet("Sheet1")
                .doWrite(getData());
    }
 
    private static List<ExcelData> getData() {
        List<ExcelData> data = new ArrayList<>();
 
        // 添加数据到工作簿，确保单元格数据类型为数值
        data.add(new ExcelData("A1", "SUM(B1:B2)")); // 公式
        data.add(new ExcelData("B1", 10)); // 数值类型
        data.add(new ExcelData("B2", 20)); // 数值类型
 
        return data;
    }
 
    public static class ExcelData {
        @ContentFontStyle(fontHeightInPoints = 10)
        private String content;
 
        public ExcelData(String content, Object value) {
            this.content = content;
            this.value = value;
        }
 
        @ContentStyle(dataFormat = 0)
        private Object value;
 
        // getters and setters
    }
}