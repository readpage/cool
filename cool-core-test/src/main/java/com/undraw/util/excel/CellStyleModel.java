package com.undraw.util.excel;

import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 *
 * @date 2023-02-09 8:58
 */

@Data
public class CellStyleModel {
    /**
     * sheet名称
     */
    private String sheetName;
    /**
     * 列索引
     */
    private int colIndex;
    /**
     * 行索引
     */
    private int rowIndex;
    /**
     * 字体名称
     */
    private String fontName;
    /**
     * 字体大小
     */
    private Double fontHeight;
    /**
     * 字体颜色
     */
    private Object fontColor;
    /**
     * 字体加粗
     */
    private Boolean fontBold;
    /**
     * 字体斜体
     */
    private Boolean fontItalic;
    /**
     * 字体下划线
     */
    private Byte fontUnderLine;
    /**
     * 字体上标下标
     */
    private Short fontTypeOffset;
    /**
     * 字体删除线
     */
    private Boolean fontStrikeout;
    /**
     * 背景颜色
     */
    private Object backgroundColor;

    /**
     * 上边框线条类型
     */
    private BorderStyle borderTop;
    /**
     * 右边框线条类型
     */
    private BorderStyle borderRight;
    /**
     * 下边框线条类型
     */
    private BorderStyle borderBottom;
    /**
     * 左边框线条类型
     */
    private BorderStyle borderLeft;
    /**
     * 上边框线条颜色
     */
    private Object topBorderColor;
    /**
     * 上边框线条颜色
     */
    private Object rightBorderColor;
    /**
     * 下边框线条颜色
     */
    private Object bottomBorderColor;
    /**
     */
    private Object leftBorderColor;
    /**
     * 水平对齐方式
     */
    private HorizontalAlignment horizontalAlignment;
    /**
     * 垂直对齐方式
     */
    private VerticalAlignment verticalAlignment;
    /**
     * 自动换行方式
     */
    private Boolean wrapText;

    /**
     * 生成字体名称样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param fontName    字体名称（默认宋体）
     * @return
     */
    public static CellStyleModel createFontNameCellStyleModel(String sheetName, int rowIndex, int columnIndex, String fontName) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, fontName, null, null, null, null, null, null, null);
    }

    /**
     * 生成字体名称大小信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param fontHeight  字体大小
     * @return
     */
    public static CellStyleModel createFontHeightCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Double fontHeight) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, fontHeight, null, null, null, null, null, null);
    }

    /**
     * 得到RBG自定义颜色
     *
     * @param redNum   红色数值
     * @param greenNum 绿色数值
     * @param blueNum  蓝色数值
     * @return
     */
    public static XSSFColor getRGBColor(int redNum, int greenNum, int blueNum) {
        XSSFColor color = new XSSFColor(new byte[]{(byte) redNum, (byte) greenNum, (byte) blueNum}, new DefaultIndexedColorMap());
        return color;
    }

    /**
     * 生成字体颜色样式信息（支持自定义RGB颜色）
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param redNum      红色数值
     * @param greenNum    绿色数值
     * @param blueNum     蓝色数值
     * @return
     */
    public static CellStyleModel createFontColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , int redNum, int greenNum, int blueNum) {
        XSSFColor fontColor = getRGBColor(redNum, greenNum, blueNum);
        return createFontColorCellStyleModel(sheetName, rowIndex, columnIndex, fontColor);
    }

    /**
     * 生成字体颜色样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param fontColor   字体颜色
     * @return
     */
    public static CellStyleModel createFontColorCellStyleModel(String sheetName, int rowIndex, int columnIndex, Object fontColor) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, null, fontColor, null, null, null, null, null);
    }

    /**
     * 生成字体加粗样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param fontBold    字体加粗
     * @return
     */
    public static CellStyleModel createFontBoldCellStyleModel(String sheetName, int rowIndex, int columnIndex, Boolean fontBold) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, fontBold, null, null, null, null);
    }

    /**
     * 生成字体斜体样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param fontItalic  字体斜体
     * @return
     */
    public static CellStyleModel createFontItalicCellStyleModel(String sheetName, int rowIndex, int columnIndex, Boolean fontItalic) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, fontItalic, null, null, null);
    }

    /**
     * 生成字体下划线样式信息
     *
     * @param sheetName     sheet页名称
     * @param rowIndex      行号
     * @param columnIndex   列号
     * @param fontUnderLine 字体下划线
     * @return
     */
    public static CellStyleModel createFontUnderLineCellStyleModel(String sheetName, int rowIndex, int columnIndex, Byte fontUnderLine) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, null, fontUnderLine, null, null);
    }

    /**
     * 生成字体上标下标样式信息
     *
     * @param sheetName      sheet页名称
     * @param rowIndex       行号
     * @param columnIndex    列号
     * @param fontTypeOffset 字体上标下标
     * @return
     */
    public static CellStyleModel createFontTypeOffsetCellStyleModel(String sheetName, int rowIndex, int columnIndex, Short fontTypeOffset) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, null, null, fontTypeOffset, null);
    }

    /**
     * 生成字体删除线样式信息
     *
     * @param sheetName     sheet页名称
     * @param rowIndex      行号
     * @param columnIndex   列号
     * @param fontStrikeout 字体删除线
     * @return
     */
    public static CellStyleModel createFontStrikeoutCellStyleModel(String sheetName, int rowIndex, int columnIndex, Boolean fontStrikeout) {
        return createFontCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, null, null, null, fontStrikeout);
    }

    /**
     * 生成字体样式信息
     *
     * @param sheetName      sheet页名称
     * @param rowIndex       行号
     * @param columnIndex    列号
     * @param fontName       字体名称（默认宋体）
     * @param fontHeight     字体大小
     * @param fontColor      字体颜色
     * @param fontBold       字体加粗
     * @param fontItalic     字体斜体
     * @param fontUnderLine  字体下划线
     * @param fontTypeOffset 字体上标下标
     * @param fontStrikeout  字体删除线
     * @return
     */
    public static CellStyleModel createFontCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , String fontName, Double fontHeight, Object fontColor, Boolean fontBold, Boolean fontItalic, Byte fontUnderLine
            , Short fontTypeOffset, Boolean fontStrikeout) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, fontName, fontHeight, fontColor, fontBold, fontItalic
                , fontUnderLine, fontTypeOffset, fontStrikeout, null);
    }

    /**
     * 生成背景颜色样式信息
     *
     * @param sheetName       sheet页名称
     * @param rowIndex        行号
     * @param columnIndex     列号
     * @param backgroundColor 背景颜色
     * @return
     */
    public static CellStyleModel createBackgroundColorCellStyleModel(String sheetName, int rowIndex, int columnIndex, Object backgroundColor) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, null, null, null, null, backgroundColor);
    }

    /**
     * 生成背景颜色样式信息（支持自定义RGB颜色）
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param redNum      红色数值
     * @param greenNum    绿色数值
     * @param blueNum     蓝色数值
     * @return
     */
    public static CellStyleModel createBackgroundColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , int redNum, int greenNum, int blueNum) {
        XSSFColor backgroundColor = getRGBColor(redNum, greenNum, blueNum);
        return createBackgroundColorCellStyleModel(sheetName, rowIndex, columnIndex, backgroundColor);
    }

    /**
     * 生成样式信息
     *
     * @param sheetName       sheet页名称
     * @param rowIndex        行号
     * @param columnIndex     列号
     * @param fontName        字体名称（宋体）
     * @param fontHeight      字体大小
     * @param fontColor       字体颜色
     * @param fontBold        字体加粗
     * @param fontItalic      字体斜体
     * @param fontUnderLine   字体下划线
     * @param fontTypeOffset  字体上标下标
     * @param fontStrikeout   字体删除线
     * @param backgroundColor 背景颜色
     * @return
     */
    public static CellStyleModel createCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , String fontName, Double fontHeight, Object fontColor, Boolean fontBold, Boolean fontItalic, Byte fontUnderLine
            , Short fontTypeOffset, Boolean fontStrikeout, Object backgroundColor) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, fontName, fontHeight, fontColor, fontBold, fontItalic
                , fontUnderLine, fontTypeOffset, fontStrikeout, backgroundColor, null, null, null, null, null, null, null, null);
    }

    /**
     * 生成上边框线条颜色样式信息
     *
     * @param sheetName      sheet页名称
     * @param rowIndex       行号
     * @param columnIndex    列号
     * @param topBorderColor 上边框线条颜色
     * @return
     */
    public static CellStyleModel createTopBorderColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Object topBorderColor) {
        return createBorderColorCellStyleModel(sheetName, rowIndex, columnIndex, topBorderColor, null, null, null);
    }

    /**
     * 生成右边框线条颜色样式信息
     *
     * @param sheetName        sheet页名称
     * @param rowIndex         行号
     * @param columnIndex      列号
     * @param rightBorderColor 右边框线条颜色
     * @return
     */
    public static CellStyleModel createRightBorderColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Object rightBorderColor) {
        return createBorderColorCellStyleModel(sheetName, rowIndex, columnIndex, null, rightBorderColor, null, null);
    }

    /**
     * 生成下边框线条颜色样式信息
     *
     * @param sheetName         sheet页名称
     * @param rowIndex          行号
     * @param columnIndex       列号
     * @param bottomBorderColor 下边框线条颜色
     * @return
     */
    public static CellStyleModel createBottomBorderColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Object bottomBorderColor) {
        return createBorderColorCellStyleModel(sheetName, rowIndex, columnIndex, null, null, bottomBorderColor, null);
    }

    /**
     * 生成左边框线条颜色样式信息
     *
     * @param sheetName       sheet页名称
     * @param rowIndex        行号
     * @param columnIndex     列号
     * @param leftBorderColor 左边框线条颜色
     * @return
     */
    public static CellStyleModel createLeftBorderColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Object leftBorderColor) {
        return createBorderColorCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, leftBorderColor);
    }

    /**
     * 生成上边框线条类型样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param borderTop   上边框线条类型
     * @return
     */
    public static CellStyleModel createTopBorderLineTypeCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderTop) {
        return createBorderLineTypeCellStyleModel(sheetName, rowIndex, columnIndex, borderTop, null, null, null);
    }

    /**
     * 生成右边框线条类型样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param borderRight 右边框线条类型
     * @return
     */
    public static CellStyleModel createRightBorderLineTypeCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderRight) {
        return createBorderLineTypeCellStyleModel(sheetName, rowIndex, columnIndex, null, borderRight, null, null);
    }

    /**
     * 生成下边框线条类型样式信息
     *
     * @param sheetName    sheet页名称
     * @param rowIndex     行号
     * @param columnIndex  列号
     * @param borderBottom 下边框线条类型
     * @return
     */
    public static CellStyleModel createBottomBorderLineTypeCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderBottom) {
        return createBorderLineTypeCellStyleModel(sheetName, rowIndex, columnIndex, null, null, borderBottom, null);
    }

    /**
     * 生成左边框线条类型样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param borderLeft  左边框线条类型
     * @return
     */
    public static CellStyleModel createLeftBorderLineTypeCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderLeft) {
        return createBorderLineTypeCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, borderLeft);
    }

    /**
     * 生成边框线条颜色样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param borderColor 边框线条颜色
     * @return
     */
    public static CellStyleModel createBorderColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Object borderColor) {
        return createBorderCellStyleModel(sheetName, rowIndex, columnIndex, null, borderColor);
    }

    /**
     * 生成边框线条颜色样式信息
     *
     * @param sheetName         sheet页名称
     * @param rowIndex          行号
     * @param columnIndex       列号
     * @param topBorderColor    上边框线条颜色
     * @param rightBorderColor  右边框线条颜色
     * @param bottomBorderColor 下边框线条颜色
     * @param leftBorderColor   左边框线条颜色
     * @return
     */
    public static CellStyleModel createBorderColorCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Object topBorderColor, Object rightBorderColor, Object bottomBorderColor, Object leftBorderColor) {
        return createBorderCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null
                , topBorderColor, rightBorderColor, bottomBorderColor, leftBorderColor);
    }

    /**
     * 生成边框线条类型样式信息
     *
     * @param sheetName      sheet页名称
     * @param rowIndex       行号
     * @param columnIndex    列号
     * @param borderLineType 边框线条类型
     * @return
     */
    public static CellStyleModel createBorderLineTypeCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderLineType) {
        return createBorderCellStyleModel(sheetName, rowIndex, columnIndex, borderLineType, null);
    }

    /**
     * 生成边框线条类型样式信息
     *
     * @param sheetName    sheet页名称
     * @param rowIndex     行号
     * @param columnIndex  列号
     * @param borderTop    上边框线条类型
     * @param borderRight  右边框线条类型
     * @param borderBottom 下边框线条类型
     * @param borderLeft   左边框线条类型
     * @return
     */
    public static CellStyleModel createBorderLineTypeCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderTop, BorderStyle borderRight, BorderStyle borderBottom, BorderStyle borderLeft) {
        return createBorderCellStyleModel(sheetName, rowIndex, columnIndex, borderTop, borderRight, borderBottom, borderLeft
                , null, null, null, null);
    }

    /**
     * 生成边框样式信息
     *
     * @param sheetName      sheet页名称
     * @param rowIndex       行号
     * @param columnIndex    列号
     * @param borderLineType 边框线条类型
     * @param borderColor    边框线条颜色
     * @return
     */
    public static CellStyleModel createBorderCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderLineType, Object borderColor) {
        return createBorderCellStyleModel(sheetName, rowIndex, columnIndex, borderLineType, borderLineType, borderLineType, borderLineType
                , borderColor, borderColor, borderColor, borderColor);
    }

    /**
     * 生成边框样式信息
     *
     * @param sheetName         sheet页名称
     * @param rowIndex          行号
     * @param columnIndex       列号
     * @param borderTop         上边框线条类型
     * @param borderRight       右边框线条类型
     * @param borderBottom      下边框线条类型
     * @param borderLeft        左边框线条类型
     * @param topBorderColor    上边框线条颜色
     * @param rightBorderColor  右边框线条颜色
     * @param bottomBorderColor 下边框线条颜色
     * @param leftBorderColor   左边框线条颜色
     * @return
     */
    public static CellStyleModel createBorderCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , BorderStyle borderTop, BorderStyle borderRight, BorderStyle borderBottom, BorderStyle borderLeft, Object topBorderColor
            , Object rightBorderColor, Object bottomBorderColor, Object leftBorderColor) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, null, null, null, null
                , null, borderTop, borderRight, borderBottom, borderLeft, topBorderColor, rightBorderColor
                , bottomBorderColor, leftBorderColor);
    }

    /**
     * 生成样式信息
     *
     * @param sheetName         sheet页名称
     * @param rowIndex          行号
     * @param columnIndex       列号
     * @param fontName          字体名称（宋体）
     * @param fontHeight        字体大小
     * @param fontColor         字体颜色
     * @param fontBold          字体加粗
     * @param fontItalic        字体斜体
     * @param fontUnderLine     字体下划线
     * @param fontTypeOffset    字体上标下标
     * @param fontStrikeout     字体删除线
     * @param backgroundColor   背景颜色
     * @param borderTop         上边框线条类型
     * @param borderRight       右边框线条类型
     * @param borderBottom      下边框线条类型
     * @param borderLeft        左边框线条类型
     * @param topBorderColor    上边框线条颜色
     * @param rightBorderColor  右边框线条颜色
     * @param bottomBorderColor 下边框线条颜色
     * @param leftBorderColor   左边框线条颜色
     * @return
     */
    public static CellStyleModel createCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , String fontName, Double fontHeight, Object fontColor, Boolean fontBold, Boolean fontItalic, Byte fontUnderLine
            , Short fontTypeOffset, Boolean fontStrikeout, Object backgroundColor, BorderStyle borderTop, BorderStyle borderRight
            , BorderStyle borderBottom, BorderStyle borderLeft, Object topBorderColor, Object rightBorderColor, Object bottomBorderColor
            , Object leftBorderColor) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, fontName, fontHeight, fontColor, fontBold, fontItalic
                , fontUnderLine, fontTypeOffset, fontStrikeout, backgroundColor, borderTop, borderRight, borderBottom
                , borderLeft, topBorderColor, rightBorderColor, bottomBorderColor, leftBorderColor, null, null);
    }

    /**
     * 生成水平对齐方式信息
     *
     * @param sheetName           sheet页名称
     * @param rowIndex            行号
     * @param columnIndex         列号
     * @param horizontalAlignment 水平对齐方式
     * @return
     */
    public static CellStyleModel createHorizontalAlignmentCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , HorizontalAlignment horizontalAlignment) {
        return createAlignmentCellStyleModel(sheetName, rowIndex, columnIndex, horizontalAlignment, null);
    }

    /**
     * 生成垂直对齐方式信息
     *
     * @param sheetName         sheet页名称
     * @param rowIndex          行号
     * @param columnIndex       列号
     * @param verticalAlignment 垂直对齐方式
     * @return
     */
    public static CellStyleModel createVerticalAlignmentCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , VerticalAlignment verticalAlignment) {
        return createAlignmentCellStyleModel(sheetName, rowIndex, columnIndex, null, verticalAlignment);
    }

    /**
     * 生成对齐方式信息
     *
     * @param sheetName           sheet页名称
     * @param rowIndex            行号
     * @param columnIndex         列号
     * @param horizontalAlignment 水平对齐方式
     * @param verticalAlignment   垂直对齐方式
     * @return
     */
    public static CellStyleModel createAlignmentCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null
                , null, null, null, null, null, null, null
                , null, null, null, null, null, null
                , horizontalAlignment, verticalAlignment);
    }

    /**
     * 生成样式信息
     *
     * @param sheetName           sheet页名称
     * @param rowIndex            行号
     * @param columnIndex         列号
     * @param fontName            字体名称（宋体）
     * @param fontHeight          字体大小
     * @param fontColor           字体颜色
     * @param fontBold            字体加粗
     * @param fontItalic          字体斜体
     * @param fontUnderLine       字体下划线
     * @param fontTypeOffset      字体上标下标
     * @param fontStrikeout       字体删除线
     * @param backgroundColor     背景颜色
     * @param borderTop           上边框线条类型
     * @param borderRight         右边框线条类型
     * @param borderBottom        下边框线条类型
     * @param borderLeft          左边框线条类型
     * @param topBorderColor      上边框线条颜色
     * @param rightBorderColor    右边框线条颜色
     * @param bottomBorderColor   下边框线条颜色
     * @param leftBorderColor     左边框线条颜色
     * @param horizontalAlignment 水平对齐方式
     * @param verticalAlignment   垂直对齐方式
     * @return
     */
    public static CellStyleModel createCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , String fontName, Double fontHeight, Object fontColor, Boolean fontBold, Boolean fontItalic, Byte fontUnderLine
            , Short fontTypeOffset, Boolean fontStrikeout, Object backgroundColor, BorderStyle borderTop, BorderStyle borderRight
            , BorderStyle borderBottom, BorderStyle borderLeft, Object topBorderColor, Object rightBorderColor, Object bottomBorderColor
            , Object leftBorderColor, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, fontName, fontHeight, fontColor, fontBold, fontItalic
                , fontUnderLine, fontTypeOffset, fontStrikeout, backgroundColor, borderTop, borderRight, borderBottom
                , borderLeft, topBorderColor, rightBorderColor, bottomBorderColor, leftBorderColor, horizontalAlignment, verticalAlignment, null);
    }

    /**
     * 生成自动换行样式信息
     *
     * @param sheetName   sheet页名称
     * @param rowIndex    行号
     * @param columnIndex 列号
     * @param wrapText    自动换行
     * @return
     */
    public static CellStyleModel createWrapTextCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , Boolean wrapText) {
        return createCellStyleModel(sheetName, rowIndex, columnIndex, null, null, null, null, null
                , null, null, null, null, null, null, null
                , null, null, null, null, null, null, null
                , wrapText);
    }

    /**
     * 生成样式信息
     *
     * @param sheetName           sheet页名称
     * @param rowIndex            行号
     * @param columnIndex         列号
     * @param fontName            字体名称（宋体）
     * @param fontHeight          字体大小
     * @param fontColor           字体颜色
     * @param fontBold            字体加粗
     * @param fontItalic          字体斜体
     * @param fontUnderLine       字体下划线
     * @param fontTypeOffset      字体上标下标
     * @param fontStrikeout       字体删除线
     * @param backgroundColor     背景颜色
     * @param borderTop           上边框线条类型
     * @param borderRight         右边框线条类型
     * @param borderBottom        下边框线条类型
     * @param borderLeft          左边框线条类型
     * @param topBorderColor      上边框线条颜色
     * @param rightBorderColor    右边框线条颜色
     * @param bottomBorderColor   下边框线条颜色
     * @param leftBorderColor     左边框线条颜色
     * @param horizontalAlignment 水平对齐方式
     * @param verticalAlignment   垂直对齐方式
     * @param wrapText            自动换行
     * @return
     */
    public static CellStyleModel createCellStyleModel(String sheetName, int rowIndex, int columnIndex
            , String fontName, Double fontHeight, Object fontColor, Boolean fontBold, Boolean fontItalic, Byte fontUnderLine
            , Short fontTypeOffset, Boolean fontStrikeout, Object backgroundColor, BorderStyle borderTop, BorderStyle borderRight
            , BorderStyle borderBottom, BorderStyle borderLeft, Object topBorderColor, Object rightBorderColor, Object bottomBorderColor
            , Object leftBorderColor, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, Boolean wrapText) {
        CellStyleModel cellStyleModel = new CellStyleModel();
        //sheet页名称
        cellStyleModel.setSheetName(sheetName);
        //行号
        cellStyleModel.setRowIndex(rowIndex);
        //列号
        cellStyleModel.setColIndex(columnIndex);

        //设置字体样式
        //字体名称（比如宋体）
        fontName = fontName != null && "".equals(fontName) ? "宋体" : fontName;
        cellStyleModel.setFontName(fontName);
        //字体大小
        fontHeight = fontHeight != null && fontHeight <= 0 ? null : fontHeight;
        cellStyleModel.setFontHeight(fontHeight);
        //字体颜色
        fontColor = fontColor != null && (fontColor instanceof IndexedColors == false && fontColor instanceof XSSFColor == false)
                ? null : fontColor;
        cellStyleModel.setFontColor(fontColor);
        //字体加粗
        cellStyleModel.setFontBold(fontBold);
        //字体斜体
        cellStyleModel.setFontItalic(fontItalic);
        //字体下划线
        fontUnderLine = fontUnderLine != null && (fontUnderLine != Font.U_NONE && fontUnderLine != Font.U_SINGLE && fontUnderLine != Font.U_DOUBLE
                && fontUnderLine != Font.U_DOUBLE_ACCOUNTING && fontUnderLine != Font.U_SINGLE_ACCOUNTING) ? null : fontUnderLine;
        cellStyleModel.setFontUnderLine(fontUnderLine);
        //字体上标下标
        fontTypeOffset = fontTypeOffset != null && (fontTypeOffset != Font.SS_NONE && fontTypeOffset != Font.SS_SUB && fontTypeOffset != Font.SS_SUPER)
                ? null : fontTypeOffset;
        cellStyleModel.setFontTypeOffset(fontTypeOffset);
        //字体删除线
        cellStyleModel.setFontStrikeout(fontStrikeout);

        //背景颜色
        backgroundColor = backgroundColor != null && (backgroundColor instanceof IndexedColors == false && backgroundColor instanceof XSSFColor == false)
                ? null : backgroundColor;
        cellStyleModel.setBackgroundColor(backgroundColor);

        //边框样式
        //上边框线条类型
        cellStyleModel.setBorderTop(borderTop);
        //右边框线条类型
        cellStyleModel.setBorderRight(borderRight);
        //下边框线条类型
        cellStyleModel.setBorderBottom(borderBottom);
        //左边框线条类型
        cellStyleModel.setBorderLeft(borderLeft);
        //上边框颜色类型
        cellStyleModel.setTopBorderColor(topBorderColor);
        //右边框颜色类型
        cellStyleModel.setRightBorderColor(rightBorderColor);
        //下边框颜色类型
        cellStyleModel.setBottomBorderColor(bottomBorderColor);
        //左边框颜色类型
        cellStyleModel.setLeftBorderColor(leftBorderColor);

        //对齐方式
        //水平对齐方式
        cellStyleModel.setHorizontalAlignment(horizontalAlignment);
        //垂直对齐方式
        cellStyleModel.setVerticalAlignment(verticalAlignment);

        //自动换行
        cellStyleModel.setWrapText(wrapText);
        return cellStyleModel;
    }
}
