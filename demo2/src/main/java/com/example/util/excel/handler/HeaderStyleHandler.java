package com.example.util.excel.handler;

import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

/**
 * 表头样式 — 灰底加粗居中带边框；内容仅垂直居中无边框
 */
public class HeaderStyleHandler extends HorizontalCellStyleStrategy {

    public HeaderStyleHandler() {
        super(createHeadStyle(), createContentStyle());
    }

    private static WriteCellStyle createHeadStyle() {
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
        return head;
    }

    private static WriteCellStyle createContentStyle() {
        WriteCellStyle content = new WriteCellStyle();
        content.setVerticalAlignment(VerticalAlignment.CENTER);
        WriteFont contentFont = new WriteFont();
        contentFont.setFontHeightInPoints((short) 10);
        content.setWriteFont(contentFont);
        return content;
    }
}
