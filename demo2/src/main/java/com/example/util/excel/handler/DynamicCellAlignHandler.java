package com.example.util.excel.handler;

import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.handler.context.CellWriteHandlerContext;
import com.example.util.excel.model.ColumnExportConfig;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * 仅对实际写入的数据单元格设置居中对齐（不触碰 setDefaultColumnStyle，避免全列 1048576 行空边框）
 */
public class DynamicCellAlignHandler implements CellWriteHandler {

    private final List<ColumnExportConfig> cols;
    private CellStyle centerStyle;

    public DynamicCellAlignHandler(List<ColumnExportConfig> cols) {
        this.cols = cols;
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext ctx) {
        if (ctx.getHead()) return;           // 跳过表头
        int colIdx = ctx.getColumnIndex();
        if (colIdx >= cols.size()) return;
        if (!"center".equals(cols.get(colIdx).getAlign())) return;

        Cell cell = ctx.getCell();
        if (cell == null) return;

        if (centerStyle == null) {
            Workbook wb = ctx.getWriteWorkbookHolder().getCachedWorkbook();
            centerStyle = wb.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 10);
            centerStyle.setFont(font);
        }
        cell.setCellStyle(centerStyle);
    }
}
