package com.example.util.excel.handler;

import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.example.util.excel.model.ColumnExportConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 按列配置设置列宽 + 表头自动筛选
 *
 * <p>继承 {@link AbstractColumnWidthStyleStrategy} 而非实现 {@link cn.idev.excel.write.handler.SheetWriteHandler}，
 * 因为 EasyExcel 在写数据时会覆盖 Sheet 创建阶段设置的列宽。
 *
 * <p>width/minWidth 单位：像素；内部转为 Excel 列宽单位（1 像素 ≈ 32，即 1 字符 ≈ 8 像素 ≈ 256 单位）。
 */
@Slf4j
public class DynamicColumnWidthHandler extends AbstractColumnWidthStyleStrategy {

    /** 像素转 Excel 列宽单位：1 像素 ≈ 32 (256/8) */
    private static final int PX_TO_EXCEL = 32;

    private final List<ColumnExportConfig> cols;
    /** 已处理过的列索引，保证每列只设一次宽 */
    private final Set<Integer> doneColumns = new HashSet<>();
    /** 是否已设置自动筛选 */
    private boolean autoFilterSet;

    public DynamicColumnWidthHandler(List<ColumnExportConfig> cols) {
        this.cols = cols;
    }

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder,
                                   List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                   Integer relativeRowIndex, Boolean isHead) {
        int colIdx = cell.getColumnIndex();
        if (colIdx >= cols.size() || doneColumns.contains(colIdx)) {
            return;
        }
        doneColumns.add(colIdx);

        ColumnExportConfig col = cols.get(colIdx);
        int w = col.getWidth() != null ? Math.max(col.getWidth() * PX_TO_EXCEL, 10 * 256)
                : col.getMinWidth() != null ? Math.max(col.getMinWidth() * PX_TO_EXCEL, 10 * 256) : 20 * 256;

        log.info("DynamicColumnWidthHandler: col[{}] width={}, minWidth={} => setColumnWidth={}",
                colIdx, col.getWidth(), col.getMinWidth(), w);

        Sheet sheet = writeSheetHolder.getSheet();
        sheet.setColumnWidth(colIdx, w);

        // 全部列处理完后启用筛选
        if (!autoFilterSet && doneColumns.size() == cols.size()) {
            autoFilterSet = true;
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, cols.size() - 1));
        }
    }
}
