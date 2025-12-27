package com.undraw.basic;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.data.*;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.handler.context.CellWriteHandlerContext;
import cn.idev.excel.write.metadata.WriteSheet;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.undraw.util.FileUtils;
import cn.undraw.util.MapUtils;
import com.undraw.domain.model.Employee;
import com.undraw.util.excel.ExcelUtils;
import com.undraw.util.excel.handler.MergeHandler;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class ExcelTest {

    @Test
    public void test() {
        String filePath = System.getProperty("user.dir") + "/upload";
        File file = new File( filePath + "/export.xlsx");

        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("number", 5.2);

        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            list.add(map);
        }

        try (ExcelWriter excelWriter = EasyExcel.write(FileUtils.createFile(filePath + "/target.xlsx")).withTemplate(file).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            excelWriter.fill(list, writeSheet);
            excelWriter.fill(list, writeSheet);
        }
    }

    @Test
    public void color() {
        String filename = System.getProperty("user.dir") + "/upload/color.xlsx";
        File file = FileUtils.createFile(filename);
        ExcelUtils.export(file, "sheet", Employee.employeeList, Employee.class, c -> {
            c.registerWriteHandler(new MergeHandler(1, new int[] {2, 3}));
            c.registerWriteHandler(new CellWriteHandler() {
                @Override
                public void afterCellDispose(CellWriteHandlerContext context) {
                    CellWriteHandler.super.afterCellDispose(context);
                }
            });
        });
    }

    @Test
    public void export() {
        String filename = System.getProperty("user.dir") + "/model/export.xlsx";
        File file = FileUtils.createFile(filename);
        ExcelUtils.export(file, "test", Employee.employeeList, Employee.class, c -> {
            c.registerWriteHandler(new MergeHandler(1, new int[] {2, 3}));
        });
    }

    @Test
    public void fill() {
        List<Map> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(MapUtils.of("name", "张三", "number", i));
        }
        String filename = System.getProperty("user.dir") + "/model/" + System.currentTimeMillis() + ".xlsx";
        String templateFileName = System.getProperty("user.dir") + "/model/export.xlsx";
        ExcelUtils.fill(new File(filename), new File(templateFileName), list, c -> {
            c.registerWriteHandler(new MergeHandler(1, 0));
        });
    }

    @Getter
    @Setter
    class WriteCellDemoData {
        private WriteCellData<String> hyperlink;

        private WriteCellData<String> comment;

        private WriteCellData<String> formula;

        private WriteCellData<String> writeCellStyle;

        private WriteCellData<String> richTest;
    }

    @Test
    public void writeCellDataWrite() {
        String filePath = System.getProperty("user.dir") + "/upload";
        String fileName = filePath + "/writeCellDataWrite" + System.currentTimeMillis() + ".xlsx";
        WriteCellDemoData writeCellDemoData = new WriteCellDemoData();

        // 设置超链接
        WriteCellData<String> hyperlink = new WriteCellData<>("官方网站");
        writeCellDemoData.setHyperlink(hyperlink);
        HyperlinkData hyperlinkData = new HyperlinkData();
        hyperlink.setHyperlinkData(hyperlinkData);
        hyperlinkData.setAddress("https://github.com/alibaba/easyexcel");
        hyperlinkData.setHyperlinkType(HyperlinkData.HyperlinkType.URL);

        // 设置备注
        WriteCellData<String> comment = new WriteCellData<>("备注的单元格信息");
        writeCellDemoData.setComment(comment);
        CommentData commentData = new CommentData();
        comment.setCommentData(commentData);
        commentData.setAuthor("Jiaju Zhuang");
        commentData.setRichTextStringData(new RichTextStringData("这是一个备注"));
        // 备注的默认大小是按照单元格的大小 这里想调整到4个单元格那么大 所以向后 向下 各额外占用了一个单元格
        commentData.setRelativeLastColumnIndex(1);
        commentData.setRelativeLastRowIndex(1);

        // 设置公式
        WriteCellData<String> formula = new WriteCellData<>();
        FormulaData formulaData = new FormulaData();
        // 将 123456789 中的第一个数字替换成 2
        // 这里只是例子 如果真的涉及到公式 能内存算好尽量内存算好 公式能不用尽量不用
        WriteFont writeFont1 = new WriteFont();
        writeFont1.setColor(IndexedColors.SKY_BLUE.getIndex());
        writeFont1.setUnderline(Font.U_SINGLE);
        WriteCellStyle writeCellStyle1 = new WriteCellStyle();
        writeCellStyle1.setWriteFont(writeFont1);
        formula.setWriteCellStyle(writeCellStyle1);
        formulaData.setFormulaValue("HYPERLINK(\"#test1!D1\", \"刘一\")");
        formula.setFormulaData(formulaData);
        writeCellDemoData.setFormula(formula);


        // 设置单个单元格的样式 当然样式 很多的话 也可以用注解等方式。
        WriteCellData<String> writeCellStyle = new WriteCellData<>("单元格样式");
        writeCellStyle.setType(CellDataTypeEnum.STRING);
        writeCellDemoData.setWriteCellStyle(writeCellStyle);
        WriteCellStyle writeCellStyleData = new WriteCellStyle();
        writeCellStyle.setWriteCellStyle(writeCellStyleData);
        // 这里需要指定 FillPatternType 为FillPatternType.SOLID_FOREGROUND 不然无法显示背景颜色.
        writeCellStyleData.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        // 背景绿色
        writeCellStyleData.setFillForegroundColor(IndexedColors.GREEN.getIndex());

        // 设置单个单元格多种样式
        // 这里需要设置 inMomery=true 不然会导致无法展示单个单元格多种样式，所以慎用
        WriteCellData<String> richTest = new WriteCellData<>();
        richTest.setType(CellDataTypeEnum.RICH_TEXT_STRING);
        writeCellDemoData.setRichTest(richTest);
        RichTextStringData richTextStringData = new RichTextStringData();
        richTest.setRichTextStringDataValue(richTextStringData);
        richTextStringData.setTextString("红色绿色默认");
        // 前2个字红色
        WriteFont writeFont = new WriteFont();
        writeFont.setColor(IndexedColors.RED.getIndex());
        richTextStringData.applyFont(0, 2, writeFont);
        // 接下来2个字绿色
        writeFont = new WriteFont();
        writeFont.setColor(IndexedColors.GREEN.getIndex());
        richTextStringData.applyFont(2, 4, writeFont);



        try (ExcelWriter excelWriter = EasyExcel.write(fileName).build()) {
            excelWriter.writeContext().writeWorkbookHolder().getWorkbook().setForceFormulaRecalculation(true);
            ExcelWriterSheetBuilder writerSheetBuilder = EasyExcel.writerSheet(0, "test").head(WriteCellDemoData.class);
            List<WriteCellDemoData> data = new ArrayList<>();
            data.add(writeCellDemoData);
            WriteSheet writeSheet = writerSheetBuilder.build();
            excelWriter.write(data, writeSheet);

            ExcelWriterSheetBuilder writerSheetBuilder1 = EasyExcel.writerSheet(1, "test1").head(WriteCellDemoData.class);
            WriteSheet writeSheet1 = writerSheetBuilder1.build();
            excelWriter.write(new ArrayList<>(), writeSheet1);
        }

    }
}
