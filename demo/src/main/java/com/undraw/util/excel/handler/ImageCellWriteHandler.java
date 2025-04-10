package com.undraw.util.excel.handler;

import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.Head;
import cn.idev.excel.metadata.data.ImageData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.write.handler.CellWriteHandler;
import cn.idev.excel.write.metadata.holder.WriteSheetHolder;
import cn.idev.excel.write.metadata.holder.WriteTableHolder;
import cn.undraw.util.StrUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImageCellWriteHandler implements CellWriteHandler {
    
    private final HashMap<String, List<ImageData>> imageDataMap = new HashMap<>(16);

    @Override
    public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, WriteCellData<?> cellData, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        if (isHead) {
            return;
        }
        // 将单元格图片数据复制出来，清空单元格图片数据
        if (!StrUtils.isEmpty(cellData.getImageDataList())) {
            imageDataMap.put(cell.getRowIndex() + "_" + cell.getColumnIndex(), cellData.getImageDataList());
            cellData.setType(CellDataTypeEnum.EMPTY);
            cellData.setImageDataList(new ArrayList<>());
        }
    }

    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        if (isHead || StrUtils.isEmpty(cellDataList)) {
            return;
        }
        String key = cell.getRowIndex() + "_" + cell.getColumnIndex();
        List<ImageData> imageDataList = imageDataMap.get(key);
        if (StrUtils.isEmpty(imageDataList)) {
            return;
        }


        // 设置单元格行高和列宽
        Sheet sheet = cell.getSheet();
        // 高 900 ≈ 60px
        sheet.getRow(cell.getRowIndex())
                .setHeight((short) 900);
        // 宽 248 * 8 = 1984 ≈ 60px
        sheet.setColumnWidth(cell.getColumnIndex(), 1984 * 9);
		// 插入图片
        for (int i = 0; i < imageDataList.size(); i++) {
            ImageData imageData = imageDataList.get(i);
            if (StrUtils.isEmpty(imageData)) {
                continue;
            }
            byte[] image = imageData.getImage();
            this.insertImage(sheet, cell, image, i);
        }
        imageDataMap.remove(key);
    }


    private void insertImage(Sheet sheet, Cell cell, byte[] pictureData, int i) {
        // 图片宽度
        int pictureWidth = Units.pixelToEMU(150);
        int index = sheet.getWorkbook().addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
        Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (StrUtils.isEmpty(drawing)) {
            drawing = sheet.createDrawingPatriarch();
        }
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        // 设置图片在哪个单元格中
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 1);
        // 设置图片在单元格中的位置
        anchor.setDx1(pictureWidth * i);
        anchor.setDx2(pictureWidth + pictureWidth * i);
        anchor.setDy1(0);
        anchor.setDy2(0);
        // 设置图片可以随着单元格移动
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        drawing.createPicture(anchor, index);
    }
}
