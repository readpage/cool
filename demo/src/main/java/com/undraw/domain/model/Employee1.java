package com.undraw.domain.model;

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.metadata.data.FormulaData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;

@Getter
@Setter
public class Employee1 extends Employee {

    @Schema(title = "姓名")
    @ExcelProperty("姓名")
    @ExcelIgnore
    private String name;

    @Schema(title = "姓名")
    @ExcelProperty(value = "姓名", index = 1)
    private WriteCellData<String> name1;

    public WriteCellData<String> getName1() {
        WriteCellData<String> writeCellData = new WriteCellData<>();

        WriteFont writeFont = new WriteFont();
        writeFont.setColor(IndexedColors.SKY_BLUE.getIndex());
        writeFont.setUnderline(Font.U_SINGLE);
        WriteCellStyle writeCellStyle = new WriteCellStyle();
        writeCellStyle.setWriteFont(writeFont);
        writeCellData.setWriteCellStyle(writeCellStyle);

        FormulaData formulaData = new FormulaData();
        formulaData.setFormulaValue(String.format("HYPERLINK(\"#student!D1\", \"%s\")", getName()));
        writeCellData.setFormulaData(formulaData);

        return writeCellData;
    }
}
