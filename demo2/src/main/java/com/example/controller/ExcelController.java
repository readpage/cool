package com.example.controller;

import cn.idev.excel.FastExcel;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.handler.SheetWriteHandler;
import cn.idev.excel.write.handler.context.CellWriteHandlerContext;
import cn.idev.excel.write.handler.context.SheetWriteHandlerContext;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import com.example.domain.entity.User;
import com.example.service.UserService;
import com.example.template.util.FilterParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel 导入导出
 */
@Tag(name = "Excel 导出")
@RestController
@RequestMapping("/excel")
public class ExcelController {

    @Resource
    private UserService userService;

    @Operation(summary = "动态表头导出 Excel")
    @PostMapping("/export")
    public void export(@RequestBody FilterParam param, HttpServletResponse response) throws IOException {
        // 1. 复用 Service 查询
        List<User> users = userService.list(param);

        // 2. 构建表头
        List<FilterParam.ColumnItem> cols = param.getColumns();
        List<List<String>> headers = cols.stream()
                .map(c -> Collections.singletonList(c.getLabel()))
                .collect(Collectors.toList());

        // 3. 构建行数据：通过实体 getter 取值
        List<List<Object>> data = users.stream()
                .map(user -> {
                    BeanWrapperImpl bw = new BeanWrapperImpl(user);
                    return cols.stream()
                            .map(c -> bw.getPropertyValue(c.getProp()))
                            .collect(Collectors.toList());
                })
                .collect(Collectors.toList());

        // 4. 设置响应头
        String filename = URLEncoder.encode("导出数据.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + filename);

        // 5. 导出
        FastExcel.write(response.getOutputStream())
                .head(headers)
                .registerWriteHandler(headerStyleStrategy())
                .registerWriteHandler(new CellAlignHandler(cols))   // 仅实际数据行设置居中
                .registerWriteHandler(new ColumnWidthHandler(cols)) // 仅列宽，不用 setDefaultColumnStyle
                .sheet("数据")
                .doWrite(data);
    }

    // ==================== 样式 ====================

    /** 表头样式 — 灰底加粗居中带边框；内容仅垂直居中无边框 */
    private HorizontalCellStyleStrategy headerStyleStrategy() {
        WriteCellStyle head = new WriteCellStyle();
        head.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        head.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        head.setHorizontalAlignment(HorizontalAlignment.CENTER);
        head.setVerticalAlignment(VerticalAlignment.CENTER);
        head.setBorderLeft(BorderStyle.THIN);
        head.setBorderRight(BorderStyle.THIN);
        head.setBorderTop(BorderStyle.THIN);
        head.setBorderBottom(BorderStyle.THIN);
        WriteFont headFont = new WriteFont();
        headFont.setBold(true);
        headFont.setFontHeightInPoints((short) 11);
        head.setWriteFont(headFont);

        WriteCellStyle content = new WriteCellStyle();
        content.setVerticalAlignment(VerticalAlignment.CENTER);
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 10);
        content.setWriteFont(contentFont);

        return new HorizontalCellStyleStrategy(head, content);
    }

    /** 仅对实际写入的数据单元格设置居中对齐（不触碰 setDefaultColumnStyle，避免全列 1048576 行空边框） */
    private static class CellAlignHandler implements CellWriteHandler {
        private final List<FilterParam.ColumnItem> cols;
        private CellStyle centerStyle;

        CellAlignHandler(List<FilterParam.ColumnItem> cols) { this.cols = cols; }

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

    /** 仅设置列宽，不使用 setDefaultColumnStyle */
    private static class ColumnWidthHandler implements SheetWriteHandler {
        private final List<FilterParam.ColumnItem> cols;
        ColumnWidthHandler(List<FilterParam.ColumnItem> cols) { this.cols = cols; }

        @Override
        public void afterSheetCreate(SheetWriteHandlerContext ctx) {
            Sheet sheet = ctx.getWriteSheetHolder().getSheet();
            for (int i = 0; i < cols.size(); i++) {
                FilterParam.ColumnItem col = cols.get(i);
                int w = col.getWidth() != null ? Math.max(col.getWidth() / 256, 10)
                        : col.getMinWidth() != null ? Math.max(col.getMinWidth() / 256, 10) : 20;
                sheet.setColumnWidth(i, w * 256);
            }
            // 表头启用筛选/排序下拉箭头
            if (!cols.isEmpty()) {
                sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, cols.size() - 1));
            }
        }
    }
}
