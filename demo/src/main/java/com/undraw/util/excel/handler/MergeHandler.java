package com.undraw.util.excel.handler;

import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * @Author: lxy
 * @CreateTime: 2024-06-12
 * @Description: EasyExcel单元格合并处理器
 */
public class MergeHandler implements CellWriteHandler {

    private int[] mergeColumnIndex;
    private int mergeRowIndex;

    public MergeHandler() {
    }

    /**
     * 构造函数
     *
     * @param mergeRowIndex     合并开始的行索引
     * @param mergeColumnIndex  要合并的列索引数组
     */
    public MergeHandler(int mergeRowIndex, int mergeColumnIndex) {
        this.mergeRowIndex = mergeRowIndex;
        this.mergeColumnIndex = new int[]{mergeColumnIndex};
    }

    /**
     * 构造函数
     *
     * @param mergeRowIndex     合并开始的行索引
     * @param mergeColumnIndex  要合并的列索引数组
     */
    public MergeHandler(int mergeRowIndex, int[] mergeColumnIndex) {
        this.mergeRowIndex = mergeRowIndex;
        this.mergeColumnIndex = mergeColumnIndex;
    }

    private Row varRow;

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        // 当前行索引
        int curRowIndex = cell.getRowIndex();

        // 填充--> 获取填充变量行数据为空修复
        if (curRowIndex == mergeRowIndex) {
            varRow = cell.getSheet().getRow(mergeRowIndex);
        }
        // <--

        // 当前列索引
        int curColIndex = cell.getColumnIndex();
        // 如果当前行大于合并开始行
        if (curRowIndex > mergeRowIndex) {
            // 当前列在需要合并的列中
            for (int columnIndex : mergeColumnIndex) {
                if (curColIndex == columnIndex) {
                    // 进行合并操作
                    mergeWithPrevRow(writeSheetHolder, cell, curRowIndex, curColIndex);
                    break;
                }
            }
        }
    }


    /**
     * 当前单元格向上合并
     *
     * @param writeSheetHolder 当前工作表持有者
     * @param cell             当前单元格
     * @param curRowIndex      当前行索引
     * @param curColIndex      当前列索引
     */
    private void mergeWithPrevRow(WriteSheetHolder writeSheetHolder, Cell cell, int curRowIndex, int curColIndex) {
        // 获取当前行的当前列的数据和上一行的当前列列数据，通过上一行数据是否相同进行合并
        Object curData = cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.getNumericCellValue();
        // 获取前一个单元格的数据
        Row row = cell.getSheet().getRow(curRowIndex - 1);

        // 填充--> 获取填充变量行数据为空修复
        if (row == null && curRowIndex - 1 == mergeRowIndex) {
            row = varRow;
        }
        // <--

        Cell preCell = row.getCell(curColIndex);
        Object preData = preCell.getCellType() == CellType.STRING ? preCell.getStringCellValue() : preCell.getNumericCellValue();

        // 判断当前单元格和前一个单元格的数据以及主键是否相同
        if (curData.equals(preData)) {
            // 获取工作表
            Sheet sheet = writeSheetHolder.getSheet();
            // 获取已合并的区域
            List<CellRangeAddress> mergeRegions = sheet.getMergedRegions();
            boolean isMerged = false;
            // 检查前一个单元格是否已经被合并
            for (int i = 0; i < mergeRegions.size() && !isMerged; i++) {
                CellRangeAddress cellRangeAddr = mergeRegions.get(i);
                // 若上一个单元格已经被合并，则先移出原有的合并单元，再重新添加合并单元
                if (cellRangeAddr.isInRange(curRowIndex - 1, curColIndex)) {
                    sheet.removeMergedRegion(i);
                    cellRangeAddr.setLastRow(curRowIndex);
                    sheet.addMergedRegion(cellRangeAddr);
                    isMerged = true;
                }
            }
            // 如果前一个单元格未被合并，则新增合并区域
            if (!isMerged) {
                CellRangeAddress cellRangeAddress = new CellRangeAddress(curRowIndex - 1, curRowIndex, curColIndex, curColIndex);
                sheet.addMergedRegion(cellRangeAddress);
            }
        }
    }


    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer integer, Integer integer1, Boolean aBoolean) {

    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer integer, Boolean aBoolean) {

    }
}