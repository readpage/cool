package com.undraw.controller;


import cn.undraw.util.log.annotation.OperateLog;
import com.undraw.util.io.ContentRange;
import com.undraw.util.io.NioUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static cn.undraw.util.log.enums.OperateTypeEnum.DOWNLOAD;

/**
 * 内容资源控制器
 */
@SuppressWarnings("unused")
@Slf4j
@RestController
@RequestMapping("/resource")
public class ResourceController {

    @Value("${file.upload}")
    private String fileDir;

    /**
     * 获取文件内容
     *
     * @param fileName 内容文件名称
     * @param response 响应对象
     */
    @GetMapping("/media/{fileName}")
    public void getMedia(@PathVariable String fileName, HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers) {
//        printRequestInfo(fileName, request, headers);

//        String filePath = MediaContentUtil.filePath();
        String filePath = "";
        try {
            this.download(fileName, filePath, request, response, headers);
        } catch (Exception e) {
            log.error("getMedia error, fileName={}", fileName, e);
        }
    }


    @Operation(summary = "下载文件")
    @OperateLog(type = DOWNLOAD)
    @GetMapping("/download")
    public void download(String filePath, HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers) {
        File file = new File(fileDir + filePath);
        String filename = file.getName();
        try {
            this.download(filename, filePath, request, response, headers);
        } catch (Exception e) {
            log.error("getMedia error, fileName={}", filename, e);
        }
    }


    // ======= internal =======

    private static void printRequestInfo(String fileName, HttpServletRequest request, HttpHeaders headers) {
        String requestUri = request.getRequestURI();
        String queryString = request.getQueryString();
        log.debug("file={}, url={}?{}", fileName, requestUri, queryString);
        log.info("headers={}", headers);
    }

    /**
     * 设置请求响应状态、头信息、内容类型与长度 等。
     * <pre>
     * <a href="https://www.rfc-editor.org/rfc/rfc7233">
     *     HTTP/1.1 Range Requests</a>
     * 2. Range Units
     * 4. Responses to a Range Request
     *
     * <a href="https://www.rfc-editor.org/rfc/rfc2616.html">
     *     HTTP/1.1</a>
     * 10.2.7 206 Partial Content
     * 14.5 Accept-Ranges
     * 14.13 Content-Length
     * 14.16 Content-Range
     * 14.17 Content-Type
     * 19.5.1 Content-Disposition
     * 15.5 Content-Disposition Issues
     *
     * <a href="https://www.rfc-editor.org/rfc/rfc2183">
     *     Content-Disposition</a>
     * 2. The Content-Disposition Header Field
     * 2.1 The Inline Disposition Type
     * 2.3 The Filename Parameter
     * </pre>
     *
     * @param response     请求响应对象
     * @param filename     请求的文件名称
     * @param contentType  内容类型
     * @param contentRange 内容范围对象
     */
    private static void setResponse(HttpServletResponse response, String filename, String contentType, ContentRange contentRange) {
        // http状态码要为206：表示获取部分内容
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        // 支持断点续传，获取部分字节内容
        // Accept-Ranges：bytes，表示支持Range请求
        response.setHeader(HttpHeaders.ACCEPT_RANGES, ContentRange.BYTES_STRING);
        // inline表示浏览器直接使用，attachment表示下载，fileName表示下载的文件名
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + URLEncoder.encode(filename, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // Content-Range，格式为：[要下载的开始位置]-[结束位置]/[文件总大小]
        // Content-Range: bytes 0-10/3103，格式为bytes 开始-结束/全部
        response.setHeader(HttpHeaders.CONTENT_RANGE, contentRange.toContentRange());

        response.setContentType(contentType);
        // Content-Length: 11，本次内容的大小
        response.setContentLengthLong(contentRange.applyAsContentLength());
    }

    /**
     * <a href="https://www.jianshu.com/p/08db5ba3bc95">
     *     Spring Boot 处理 HTTP Headers</a>
     */
    private void download(String fileName, String path, HttpServletRequest request, HttpServletResponse response, HttpHeaders headers) {
        Path filePath = Paths.get(path + fileName);
        if (!Files.exists(filePath)) {
            log.warn("file not exist, filePath={}", filePath);
            return;
        }
        long fileLength = 0;
        try {
            fileLength = Files.size(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        long fileLength2 = filePath.toFile().length() - 1;
//        // fileLength=1184856, fileLength2=1184855
//        log.info("fileLength={}, fileLength2={}", fileLength, fileLength2);

        // 内容范围
        ContentRange contentRange = applyAsContentRange(headers, fileLength, request);

        // 要下载的长度
        long contentLength = contentRange.applyAsContentLength();
        log.debug("contentRange={}, contentLength={}", contentRange, contentLength);

        // 文件类型
        String contentType = request.getServletContext().getMimeType(fileName);
        // mimeType=video/mp4, CONTENT_TYPE=null
        log.debug("mimeType={}, CONTENT_TYPE={}", contentType, request.getContentType());

        setResponse(response, fileName, contentType, contentRange);

        // 耗时指标统计
        StopWatch stopWatch = new StopWatch("downloadFile");
        stopWatch.start(fileName);
        try {
            // case-1.参考网上他人的实现
//            if (fileLength >= Integer.MAX_VALUE) {
//                NioUtils.copy(filePath, response, contentRange);
//            } else {
//                NioUtils.copyByChannelAndBuffer(filePath, response, contentRange);
//            }

            // case-2.使用现成API
            NioUtils.copyByBio(filePath, response, contentRange);
//            NioUtils.copyByNio(filePath, response, contentRange);

            // case-3.视频分段渐进式播放
//            if (contentType.startsWith("video")) {
//                NioUtils.copyForBufferSize(filePath, response, contentRange);
//            } else {
//                // 图片、PDF等文件
//                NioUtils.copyByBio(filePath, response, contentRange);
//            }
        } finally {
            stopWatch.stop();
            log.info("download file, fileName={}, time={} ms", fileName, stopWatch.getTotalTimeMillis());
        }
    }

    private static ContentRange applyAsContentRange(HttpHeaders headers, long fileLength, HttpServletRequest request) {
        /*
         * 3.1. Range - HTTP/1.1 Range Requests
         * https://www.rfc-editor.org/rfc/rfc7233#section-3.1
         * Range: "bytes" "=" first-byte-pos "-" [ last-byte-pos ]
         *
         * For example:
         * bytes=0-
         * bytes=0-499
         */
        // Range：告知服务端，客户端下载该文件想要从指定的位置开始下载
        List<HttpRange> httpRanges = headers.getRange();

        String range = request.getHeader(HttpHeaders.RANGE);
        // httpRanges=[], range=null
        // httpRanges=[448135688-], range=bytes=448135688-
        log.debug("httpRanges={}, range={}", httpRanges, range);

        // 开始下载位置
        long firstBytePos;
        // 结束下载位置
        long lastBytePos;
        if (CollectionUtils.isEmpty(httpRanges)) {
            firstBytePos = 0;
            lastBytePos = fileLength - 1;
        } else {
            HttpRange httpRange = httpRanges.get(0);
            firstBytePos = httpRange.getRangeStart(fileLength);
            lastBytePos = httpRange.getRangeEnd(fileLength);
        }
        return new ContentRange(firstBytePos, lastBytePos, fileLength);
    }
}

