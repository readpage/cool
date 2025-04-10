package com.undraw.util.excel;

/**
 * 
 * @date 2023-02-09 9:15
 */

import cn.idev.excel.write.handler.AbstractRowWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import cn.undraw.util.StrUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义单元格样式处理器（支持字体样式、背景颜色、边框样式、对齐方式、自动换行）
 */
public class CustomCellStyleHandler extends AbstractRowWriteHandler {
    /**
     * sheet页名称列表
     */
    private List<String> sheetNameList;
    /**
     * 样式信息
     */
    private List<CellStyleModel> cellStyleList = new ArrayList<>();

    /**
     * 自定义样式适配器构造方法
     *
     * @param cellStyleList 样式信息
     */
    public CustomCellStyleHandler(List<CellStyleModel> cellStyleList) {
        if (StrUtils.isEmpty(cellStyleList)) {
            return;
        }
        cellStyleList = cellStyleList.stream().filter(x -> x != null
                //判断sheet名称KEY是否存在
                && StrUtils.isNotEmpty(x.getSheetName())
                //字体样式
                //判断字体颜色KEY是否存在
                && (x.getFontColor() == null || x.getFontColor() instanceof IndexedColors
                || x.getFontColor() instanceof XSSFColor)
                //判断背景颜色KEY是否存在
                && (x.getBackgroundColor() == null || x.getBackgroundColor() instanceof IndexedColors
                || x.getBackgroundColor() instanceof XSSFColor)
                //边框样式
                // 判断上边框线条颜色KEY是否存在
                && (x.getTopBorderColor() == null || x.getTopBorderColor() instanceof IndexedColors
                || x.getTopBorderColor() instanceof XSSFColor)
                // 判断右边框线条颜色KEY是否存在
                && (x.getRightBorderColor() == null || x.getRightBorderColor() instanceof IndexedColors
                || x.getRightBorderColor() instanceof XSSFColor)
                // 判断下边框线条颜色KEY是否存在
                && (x.getBottomBorderColor() == null || x.getBottomBorderColor() instanceof IndexedColors
                || x.getBottomBorderColor() instanceof XSSFColor)
                // 判断左边框线条颜色KEY是否存在
                && (x.getLeftBorderColor() == null || x.getLeftBorderColor() instanceof IndexedColors
                || x.getLeftBorderColor() instanceof XSSFColor)
        ).collect(Collectors.toList());
        this.cellStyleList = cellStyleList;
        sheetNameList = this.cellStyleList.stream().map(x -> x.getSheetName()).distinct().collect(Collectors.toList());
    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row
            , Integer relativeRowIndex, Boolean isHead) {
        Sheet sheet = writeSheetHolder.getSheet();
        //不需要添加样式，或者当前sheet页不需要添加样式
        if (cellStyleList == null || cellStyleList.size() <= 0 || sheetNameList.contains(sheet.getSheetName()) == false) {
            return;
        }
        //获取当前行的样式信息
        List<CellStyleModel> rowCellStyleList = cellStyleList.stream().filter(x ->
                x.getSheetName().equals(sheet.getSheetName()) && x.getRowIndex() == relativeRowIndex).collect(Collectors.toList());
        //该行不需要设置样式
        if (rowCellStyleList == null || rowCellStyleList.size() <= 0) {
            return;
        }
        for (CellStyleModel cellStyleModel : rowCellStyleList) {
            //设置单元格样式
            setCellStyle(cellStyleModel, row);
        }
        //删除已添加的样式信息
        cellStyleList.removeAll(rowCellStyleList);
        //重新获取要添加的sheet页姓名
        sheetNameList = cellStyleList.stream().map(x -> x.getSheetName()).distinct().collect(Collectors.toList());
    }

    /**
     * 给单元格设置样式
     *
     * @param cellStyleModel 样式信息
     * @param row            行对象
     */
    private void setCellStyle(CellStyleModel cellStyleModel, Row row) {
        //背景颜色
        Object backgroundColor = cellStyleModel.getBackgroundColor();
        //自动换行
        Boolean wrapText = cellStyleModel.getWrapText();
        //列索引
        int colIndex = cellStyleModel.getColIndex();
        //边框样式
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex);
        }
        XSSFCellStyle style = (XSSFCellStyle) cell.getRow().getSheet().getWorkbook().createCellStyle();
        // 克隆出一个 style
        style.cloneStyleFrom(cell.getCellStyle());
        //设置背景颜色
        if (backgroundColor != null) {
            //使用IndexedColors定义的颜色
            if (backgroundColor instanceof IndexedColors) {
                style.setFillForegroundColor(((IndexedColors) backgroundColor).getIndex());
            }
            //使用自定义的RGB颜色
            else if (backgroundColor instanceof XSSFColor) {
                style.setFillForegroundColor((XSSFColor) backgroundColor);
            }
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        //设置自动换行
        if (wrapText != null) {
            style.setWrapText(wrapText);
        }
        //设置字体样式
        setFontStyle(row, style, cellStyleModel);
        //设置边框样式
        setBorderStyle(style, cellStyleModel);
        //设置对齐方式
        setAlignmentStyle(style, cellStyleModel);
        cell.setCellStyle(style);
    }

    /**
     * 设置字体样式
     *
     * @param row            行对象
     * @param style          单元格样式
     * @param cellStyleModel 样式信息
     */
    private void setFontStyle(Row row, XSSFCellStyle style, CellStyleModel cellStyleModel) {
        //字体名称
        String fontName = cellStyleModel.getFontName();
        //字体大小
        Double fontHeight = cellStyleModel.getFontHeight();
        //字体颜色
        Object fontColor = cellStyleModel.getFontColor();
        //字体加粗
        Boolean fontBold = cellStyleModel.getFontBold();
        //字体斜体
        Boolean fontItalic = cellStyleModel.getFontItalic();
        //字体下划线
        Byte fontUnderLine = cellStyleModel.getFontUnderLine();
        //字体上标下标
        Short fontTypeOffset = cellStyleModel.getFontTypeOffset();
        //字体删除线
        Boolean fontStrikeout = cellStyleModel.getFontStrikeout();
        //不需要设置字体样式
        if (fontName == null && fontHeight == null && fontColor == null && fontBold == null && fontItalic == null
                && fontUnderLine == null && fontTypeOffset == null && fontStrikeout == null) {
            return;
        }
        XSSFFont font = null;
        //样式存在字体对象时，使用原有的字体对象
        if (style.getFontIndex() != 0) {
            font = style.getFont();
        }
        //样式不存在字体对象时，创建字体对象
        else {
            font = (XSSFFont) row.getSheet().getWorkbook().createFont();
            //默认字体为宋体
            font.setFontName("宋体");
        }
        //设置字体名称
        if (fontName != null) {
            font.setFontName(fontName);
        }
        //设置字体大小
        if (fontHeight != null) {
            font.setFontHeight(fontHeight);
        }
        //设置字体颜色
        if (fontColor != null) {
            //使用IndexedColors定义的颜色
            if (fontColor instanceof IndexedColors) {
                font.setColor(((IndexedColors) fontColor).getIndex());
            }
            //使用自定义的RGB颜色
            else if (fontColor instanceof XSSFColor) {
                font.setColor((XSSFColor) fontColor);
            }
        }
        //设置字体加粗
        if (fontBold != null) {
            font.setBold(fontBold);
        }
        //设置字体斜体
        if (fontItalic != null) {
            font.setItalic(fontItalic);
        }
        //设置字体下划线
        if (fontUnderLine != null) {
            font.setUnderline(fontUnderLine);
        }
        //设置字体上标下标
        if (fontTypeOffset != null) {
            font.setTypeOffset(fontTypeOffset);
        }
        //设置字体删除线
        if (fontStrikeout != null) {
            font.setStrikeout(fontStrikeout);
        }
        style.setFont(font);
    }

    /**
     * 设置边框样式
     *
     * @param style          单元格样式
     * @param cellStyleModel 样式信息
     */
    private void setBorderStyle(XSSFCellStyle style, CellStyleModel cellStyleModel) {
        //上边框线条类型
        BorderStyle borderTop = cellStyleModel.getBorderTop();
        //右边框线条类型
        BorderStyle borderRight = cellStyleModel.getBorderRight();
        //下边框线条类型
        BorderStyle borderBottom = cellStyleModel.getBorderBottom();
        //左边框线条类型
        BorderStyle borderLeft = cellStyleModel.getBorderLeft();
        //上边框颜色类型
        Object topBorderColor = cellStyleModel.getTopBorderColor();
        //右边框颜色类型
        Object rightBorderColor = cellStyleModel.getRightBorderColor();
        //下边框颜色类型
        Object bottomBorderColor = cellStyleModel.getBottomBorderColor();
        //左边框颜色类型
        Object leftBorderColor = cellStyleModel.getLeftBorderColor();
        //不需要设置边框样式
        if (borderTop == null && borderRight == null && borderBottom == null && borderLeft == null && topBorderColor == null
                && rightBorderColor == null && bottomBorderColor == null && leftBorderColor == null) {
            return;
        }
        //设置上边框线条类型
        if (borderTop != null) {
            style.setBorderTop(borderTop);
        }
        //设置右边框线条类型
        if (borderRight != null) {
            style.setBorderRight(borderRight);
        }
        //设置下边框线条类型
        if (borderBottom != null) {
            style.setBorderBottom(borderBottom);
        }
        //设置左边框线条类型
        if (borderLeft != null) {
            style.setBorderLeft(borderLeft);
        }
        //设置上边框线条颜色
        if (topBorderColor != null) {
            //使用IndexedColors定义的颜色
            if (topBorderColor instanceof IndexedColors) {
                style.setTopBorderColor(((IndexedColors) topBorderColor).getIndex());
            }
            //使用自定义的RGB颜色
            else if (topBorderColor instanceof XSSFColor) {
                style.setTopBorderColor((XSSFColor) topBorderColor);
            }
        }
        //设置右边框线条颜色
        if (rightBorderColor != null) {
            //使用IndexedColors定义的颜色
            if (rightBorderColor instanceof IndexedColors) {
                style.setRightBorderColor(((IndexedColors) rightBorderColor).getIndex());
            }
            //使用自定义的RGB颜色
            else if (rightBorderColor instanceof XSSFColor) {
                style.setRightBorderColor((XSSFColor) rightBorderColor);
            }
        }
        //设置下边框线条颜色
        if (bottomBorderColor != null) {
            //使用IndexedColors定义的颜色
            if (bottomBorderColor instanceof IndexedColors) {
                style.setBottomBorderColor(((IndexedColors) bottomBorderColor).getIndex());
            }
            //使用自定义的RGB颜色
            else if (bottomBorderColor instanceof XSSFColor) {
                style.setBottomBorderColor((XSSFColor) bottomBorderColor);
            }
        }
        //设置左边框线条颜色
        if (leftBorderColor != null) {
            //使用IndexedColors定义的颜色
            if (leftBorderColor instanceof IndexedColors) {
                style.setLeftBorderColor(((IndexedColors) leftBorderColor).getIndex());
            }
            //使用自定义的RGB颜色
            else if (topBorderColor instanceof XSSFColor) {
                style.setLeftBorderColor((XSSFColor) leftBorderColor);
            }
        }
    }

    /**
     * 设置对齐方式
     *
     * @param style          单元格样式
     * @param cellStyleModel 样式信息
     */
    private void setAlignmentStyle(XSSFCellStyle style, CellStyleModel cellStyleModel) {
        //水平对齐方式
        HorizontalAlignment horizontalAlignment = cellStyleModel.getHorizontalAlignment();
        //垂直对齐方式
        VerticalAlignment verticalAlignment = cellStyleModel.getVerticalAlignment();
        //不需要设置对齐方式
        if (horizontalAlignment == null && verticalAlignment == null) {
            return;
        }
        //设置水平对齐方式
        if (horizontalAlignment != null) {
            style.setAlignment(horizontalAlignment);
        }
        //设置垂直对齐方式
        if (verticalAlignment != null) {
            style.setVerticalAlignment(verticalAlignment);
        }
    }
}