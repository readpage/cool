package com.example.util.excel.handler;

import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.handler.context.CellWriteHandlerContext;
import com.example.util.excel.model.ColumnExportConfig;
import org.apache.poi.ss.usermodel.*;

import java.util.List;

/**
 * 按列配置的 align 对齐数据单元格（left / center / right），
 * 不触碰 setDefaultColumnStyle，避免全列 1048576 行空边框。
 */
public class DynamicCellAlignHandler implements CellWriteHandler {

    private final List<ColumnExportConfig> cols;
    private CellStyle leftStyle;
    private CellStyle centerStyle;
    private CellStyle rightStyle;

    public DynamicCellAlignHandler(List<ColumnExportConfig> cols) {
        this.cols = cols;
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext ctx) {
        if (ctx.getHead()) return;           // 跳过表头
        int colIdx = ctx.getColumnIndex();
        if (colIdx >= cols.size()) return;

        String align = cols.get(colIdx).getAlign();
        CellStyle target = getOrCreateStyle(ctx, align);
        if (target == null) return;          // 未指定对齐或未知值 → 保持 Excel 默认

        Cell cell = ctx.getCell();
        if (cell == null) return;
        cell.setCellStyle(target);
    }

    private CellStyle getOrCreateStyle(CellWriteHandlerContext ctx, String align) {
        if (align == null) return null;
        switch (align.toLowerCase()) {
            case "left":
                if (leftStyle == null) leftStyle = createStyle(ctx, HorizontalAlignment.LEFT);
                return leftStyle;
            case "center":
                if (centerStyle == null) centerStyle = createStyle(ctx, HorizontalAlignment.CENTER);
                return centerStyle;
            case "right":
                if (rightStyle == null) rightStyle = createStyle(ctx, HorizontalAlignment.RIGHT);
                return rightStyle;
            default:
                return null;
        }
    }

    private CellStyle createStyle(CellWriteHandlerContext ctx, HorizontalAlignment hAlign) {
        Workbook wb = ctx.getWriteWorkbookHolder().getCachedWorkbook();
        CellStyle style = wb.createCellStyle();
        style.setAlignment(hAlign);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }
}
