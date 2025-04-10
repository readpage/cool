package com.undraw.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * 图片压缩工具类
 * @author lnj
 * createTime 2018-10-19 15:31
 **/
@Slf4j
public class ImageUtils {

    // 图片默认缩放比率
    private static final float DEFAULT_SCALE = 1f;

    // 缩略图后缀
    private static final String SUFFIX = "-preview";


    /**
     * @Anthor readpage
     * 压缩图片大小
     * path -- 地址
     * scale -- 指定图片的大小，值在0到1之间，1f就是原图大小，0.5就是原图的一半大小，这里的大小是指图片的长宽。
     * quality -- 图片的质量，值也是在0到1，越接近于1质量越好，越接近于0质量越差。 输出质量 默认是0.75
     **/
    public static void compressSize2(String path) {
        String fileDir = path.substring(0, path.lastIndexOf("/"));
        try {
            Thumbnails.of(path)
                    .scale(DEFAULT_SCALE)
                    .outputQuality(0.75)
                    .toFile(fileDir + "/preview.jpg");
        } catch (IOException e) {
            throw new RuntimeException("压缩图片错误!");
        }
    }

    /**
     * @Anthor readpage
     * 压缩图片大小
     * oldPath -- 原地址
     * newPath -- 新地址
     * scale -- 指定图片的大小，值在0到1之间，1f就是原图大小，0.5就是原图的一半大小，这里的大小是指图片的长宽。
     * quality -- 图片的质量，值也是在0到1，越接近于1质量越好，越接近于0质量越差。 输出质量 默认是0.75
     **/
    public static void compressSize2(String oldPath, String newPath, float scale, float quality) {
        try {
            Thumbnails.of(oldPath)
                    .scale(scale)
                    .outputQuality(quality)
                    .toFile(newPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 按文件大小压缩一个图片字节数组到本地文件
     * @param destPath 目标图片路径
     * @param confineSize 限制文件大小 kb
     * @return
     */
    public static void compressSize(String destPath, int confineSize){
        String prefix = destPath.substring(0, destPath.lastIndexOf("."));
        String suffix = destPath.substring(destPath.lastIndexOf("."));
        String toPath = prefix + SUFFIX + suffix;
        compressSize(destPath, toPath, confineSize);
    }


    /**
     * 按文件大小压缩一个图片字节数组到本地文件
     * @param destPath 目标图片路径
     * @param toPath 目标文件名称路径
     * @param confineSize 限制文件大小 kb
     * @return
     */
    public static void compressSize(String destPath, String toPath, int confineSize){
        File destFile = new File(destPath);
        try {
            byte[] bytes = compressScaleToBytes(FileUtils.readFileToByteArray(destFile), confineSize, destFile.getName());
            OutputStream out = new FileOutputStream(toPath);
            out.write(bytes);
            out.flush();
            IOUtils.closeQuietly(out);
            out = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据指定大小压缩图片
     *
     * @param imageBytes  源图片字节数组
     * @param desFileSize 指定图片大小，单位kb
     * @param imageName     影像名称
     * @return 压缩质量后的图片字节数组
     */
    public static byte[] compressScaleToBytes(byte[] imageBytes, long desFileSize, String imageName) {
        if (imageBytes == null || imageBytes.length <= 0 || imageBytes.length < desFileSize * 1024) {
            return imageBytes;
        }
        long srcSize = imageBytes.length;
        try {
            while (imageBytes.length > desFileSize * 1024) {
                double quality = getQuality(imageBytes.length / 1024);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(imageBytes.length);
                Thumbnails.of(inputStream)
                        .scale(quality)
                        .outputQuality(quality)
                        .toOutputStream(outputStream);
                Thumbnails.of(inputStream);
                imageBytes = outputStream.toByteArray();
                //关闭流
                IOUtils.closeQuietly(inputStream,outputStream);
                inputStream = null;
                outputStream = null;
            }
            log.info("【图片压缩】imageName={} | 图片原大小={}kb | 压缩后大小={}kb",
                    imageName, srcSize / 1024, imageBytes.length / 1024);
        } catch (Exception e) {
            log.error("【图片压缩】msg=图片压缩失败!", e);
        }
        return imageBytes;
    }

    /**
     * 自动调节精度(经验数值)
     * @param size 源图片大小 kb
     * @return 图片压缩质量比
     */
    private static double getQuality(long size) {
        if (size < 5000) {
            return 0.95;
        } else if (size > 2000) {
            return 0.7;
        } else if (size < 1000) {
            return 0.6;
        }
        return  0.5;
    }

}