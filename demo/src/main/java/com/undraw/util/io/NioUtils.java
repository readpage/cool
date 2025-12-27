package com.undraw.util.io;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.NioUtil;
import cn.hutool.core.io.StreamProgress;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class NioUtils {


    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // 这里可以记录日志或者进行其他处理，例如抛出RuntimeException或者不做任何处理
                // throw new RuntimeException(e);
            }
        }
    }

    /**
     * 缓冲区大小 16KB = 16 * 1024 bytes = 16384 bytes
     */
    private static final int BUFFER_SIZE = 16384;

    /**
     * <pre>
     * <a href="https://blog.csdn.net/qq_32099833/article/details/109703883">
     *     Java后端实现视频分段渐进式播放</a>
     * 服务端如何将一个大的视频文件做切分，分段响应给客户端，让浏览器可以渐进式地播放。
     * 文件的断点续传、文件多线程并发下载（迅雷就是这么玩的）等。
     *
     * <a href="https://blog.csdn.net/qq_32099833/article/details/109630499">
     *     大文件分片上传前后端实现</a>
     * </pre>
     */
    public static void copyForBufferSize(Path filePath, HttpServletResponse response, ContentRange contentRange) {
        String fileName = filePath.getFileName().toString();

        RandomAccessFile randomAccessFile = null;
        OutputStream outputStream = null;
        try {
            // 随机读文件
            randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
            // 移动访问指针到指定位置
            randomAccessFile.seek(contentRange.getStart());

            // 注意：缓冲区大小 2MB，视频加载正常；1MB时有部分视频加载失败
            int bufferSize = BUFFER_SIZE;

            //获取响应的输出流
            outputStream = new BufferedOutputStream(response.getOutputStream(), bufferSize);

            // 每次请求只返回1MB的视频流
            byte[] buffer = new byte[bufferSize];
            int len = randomAccessFile.read(buffer);
            //设置此次相应返回的数据长度
            response.setContentLength(len);
            // 将这1MB的视频流响应给客户端
            outputStream.write(buffer, 0, len);

            log.info("file download complete, fileName={}, contentRange={}",
                    fileName, contentRange.toContentRange());
        } catch (ClientAbortException e) {
            // 捕获此异常表示用户停止下载
            log.warn("client stop file download, fileName={}", fileName);
        } catch (Exception e) {
            log.error("file download error, fileName={}", fileName, e);
        } finally {
            close(outputStream);
            close(randomAccessFile);
        }
    }

    /**
     * 拷贝流，拷贝后关闭流。
     *
     * @param filePath     源文件路径
     * @param response     请求响应
     * @param contentRange 内容范围
     */
    public static void copyByBio(Path filePath, HttpServletResponse response, ContentRange contentRange) {
        String fileName = filePath.getFileName().toString();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
            randomAccessFile.seek(contentRange.getStart());

            inputStream = Channels.newInputStream(randomAccessFile.getChannel());
            outputStream = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE);

            StreamProgress streamProgress = new StreamProgressImpl(fileName);

            long transmitted = IoUtil.copy(inputStream, outputStream, BUFFER_SIZE, streamProgress);
            log.info("file download complete, fileName={}, transmitted={}", fileName, transmitted);
        } catch (ClientAbortException e) {
            // 捕获此异常表示用户停止下载
            log.warn("client stop file download, fileName={}", fileName);
        } catch (Exception e) {
            log.error("file download error, fileName={}", fileName, e);
        } finally {
            close(outputStream);
            close(inputStream);
        }
    }


    /**
     * 拷贝流，拷贝后关闭流。
     * <pre>
     * <a href="https://www.cnblogs.com/czwbig/p/10035631.html">
     *     Java NIO 学习笔记（一）----概述，Channel/Buffer</a>
     * </pre>
     *
     * @param filePath     源文件路径
     * @param response     请求响应
     * @param contentRange 内容范围
     */
    public static void copyByNio(Path filePath, HttpServletResponse response, ContentRange contentRange) {
        String fileName = filePath.getFileName().toString();

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
            randomAccessFile.seek(contentRange.getStart());

            inputStream = Channels.newInputStream(randomAccessFile.getChannel());
            outputStream = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE);

            StreamProgress streamProgress = new StreamProgressImpl(fileName);

            long transmitted = NioUtil.copyByNIO(inputStream, outputStream,
                    BUFFER_SIZE, streamProgress);
            log.info("file download complete, fileName={}, transmitted={}", fileName, transmitted);
        } catch (ClientAbortException | IORuntimeException e) {
            // 捕获此异常表示用户停止下载
            log.warn("client stop file download, fileName={}", fileName);
        } catch (Exception e) {
            log.error("file download error, fileName={}", fileName, e);
        } finally {
            IoUtil.close(outputStream);
            IoUtil.close(inputStream);
        }
    }

    /**
     * <pre>
     * <a href="https://blog.csdn.net/lovequanquqn/article/details/104562945">
     *     SpringBoot Java实现Http方式分片下载断点续传+实现H5大视频渐进式播放</a>
     * SpringBoot 实现Http分片下载断点续传，从而实现H5页面的大视频播放问题，实现渐进式播放，每次只播放需要播放的内容就可以了，不需要加载整个文件到内存中。
     * 二、Http分片下载断点续传实现
     * 四、缓存文件定时删除任务
     * </pre>
     */
    public static void copy(Path filePath, HttpServletResponse response, ContentRange contentRange) {
        String fileName = filePath.getFileName().toString();
        // 要下载的长度
        long contentLength = contentRange.applyAsContentLength();

        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        // 已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
            randomAccessFile.seek(contentRange.getStart());
            outputStream = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE);
            // 把数据读取到缓冲区中
            byte[] buffer = new byte[BUFFER_SIZE];

            int len = BUFFER_SIZE;
            //warning：判断是否到了最后不足4096（buffer的length）个byte这个逻辑（(transmitted + len) <= contentLength）要放前面
            //不然会会先读取randomAccessFile，造成后面读取位置出错;
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
                transmitted += len;

                log.info("fileName={}, transmitted={}", fileName, transmitted);
            }
            //处理不足buffer.length部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buffer, 0, (int) (contentLength - transmitted));
                outputStream.write(buffer, 0, len);
                transmitted += len;

                log.info("fileName={}, transmitted={}", fileName, transmitted);
            }

            log.info("file download complete, fileName={}, transmitted={}", fileName, transmitted);
        } catch (ClientAbortException e) {
            // 捕获此异常表示用户停止下载
            log.warn("client stop file download, fileName={}, transmitted={}", fileName, transmitted);
        } catch (Exception e) {
            log.error("file download error, fileName={}, transmitted={}", fileName, transmitted, e);
        } finally {
            IoUtil.close(outputStream);
            IoUtil.close(randomAccessFile);
        }
    }

    /**
     * 通过数据传输通道和缓冲区读取文件数据。
     * <pre>
     * 当文件长度超过{@link Integer#MAX_VALUE}时，
     * 使用{@link FileChannel#map(FileChannel.MapMode, long, long)}报如下异常。
     * java.lang.IllegalArgumentException: Size exceeds Integer.MAX_VALUE
     *   at sun.nio.ch.FileChannelImpl.map(FileChannelImpl.java:863)
     *   at com.example.insurance.controller.ResourceController.download(ResourceController.java:200)
     * </pre>
     *
     * @param filePath     源文件路径
     * @param response     请求响应
     * @param contentRange 内容范围
     */
    public static void copyByChannelAndBuffer(Path filePath, HttpServletResponse response, ContentRange contentRange) {
        String fileName = filePath.getFileName().toString();
        // 要下载的长度
        long contentLength = contentRange.applyAsContentLength();

        BufferedOutputStream outputStream = null;
        FileChannel inChannel = null;
        // 已传送数据大小
        long transmitted = 0;
        long firstBytePos = contentRange.getStart();
        long fileLength = contentRange.getLength();
        try {
            inChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
            // 建立直接缓冲区
            MappedByteBuffer inMap = inChannel.map(FileChannel.MapMode.READ_ONLY, firstBytePos, fileLength);
            outputStream = new BufferedOutputStream(response.getOutputStream(), BUFFER_SIZE);
            // 把数据读取到缓冲区中
            byte[] buffer = new byte[BUFFER_SIZE];

            int len = BUFFER_SIZE;
            // warning：判断是否到了最后不足4096（buffer的length）个byte这个逻辑（(transmitted + len) <= contentLength）要放前面
            // 不然会会先读取file，造成后面读取位置出错
            while ((transmitted + len) <= contentLength) {
                inMap.get(buffer);
                outputStream.write(buffer, 0, len);
                transmitted += len;

                log.info("fileName={}, transmitted={}", fileName, transmitted);
            }
            // 处理不足buffer.length部分
            if (transmitted < contentLength) {
                len = (int) (contentLength - transmitted);
                buffer = new byte[len];
                inMap.get(buffer);
                outputStream.write(buffer, 0, len);
                transmitted += len;

                log.info("fileName={}, transmitted={}", fileName, transmitted);
            }

            log.info("file download complete, fileName={}, transmitted={}", fileName, transmitted);
        } catch (ClientAbortException e) {
            // 捕获此异常表示用户停止下载
            log.warn("client stop file download, fileName={}, transmitted={}", fileName, transmitted);
        } catch (Exception e) {
            log.error("file download error, fileName={}, transmitted={}", fileName, transmitted, e);
        } finally {
            close(outputStream);
            close(inChannel);
        }
    }
}
