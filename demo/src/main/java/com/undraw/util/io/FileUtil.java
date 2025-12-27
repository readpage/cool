package com.undraw.util.io;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.StrUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLEncoder;
import java.util.Date;

@Component
@Slf4j
public class FileUtil {

    @Value("${file.upload:}")
    private String uploadPath;

    public void download(String objectName, HttpServletRequest request, HttpServletResponse response) {
        File file = new File(uploadPath + File.separator + objectName);
        if (!file.exists()) {
            throw new CustomerException(file.getName() + "文件不存在!");
        }
        String contentType = request.getServletContext().getMimeType(objectName);
        //设置内容类型
        response.setContentType(contentType);
        // 资源在客户端被缓存后，可在 259,200 秒（即 3 天）‌ 内直接使用，无需向服务器重新请求
        response.addHeader(HttpHeaders.CACHE_CONTROL,"max-age=259200");
        response.setHeader("Last-Modified", new Date().toString());
        RandomAccessFile randomAccessFile;
        try {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + URLEncoder.encode(file.getName(), "UTF-8"));
            // 随机读文件
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (contentType.startsWith("video/") || contentType.startsWith("audio/")) {
            OutputStream outputStream = null;
            try {
                long fileLength = randomAccessFile.length();
                //获取从那个字节开始读取文件
                String rangeString = request.getHeader("Range");
                long range = 0;
                // Accept-Ranges：bytes，表示支持Range请求
                response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
                if (StrUtils.isNotEmpty(rangeString)) {
                    range = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
                    //设置此次相应返回的数据范围
                    response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + range + "-" + (fileLength - 1) + "/" + fileLength);
                    //返回码需要为206，代表只处理了部分请求，响应了部分数据
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                } else {
                    response.setHeader("Content-length", fileLength + "");
                }

                //获取响应的输出流
                outputStream = response.getOutputStream();
                // 移动访问指针到指定位置
                randomAccessFile.seek(range);
                // 每次请求只返回1MB的视频流
                byte[] bytes = new byte[1024 * 1024];
                int len = randomAccessFile.read(bytes);
                if (StrUtils.isNotEmpty(rangeString)) {
                    //设置此次相应返回的数据长度
                    response.setContentLength(len);
                }
                // 将这1MB的视频流响应给客户端
                outputStream.write(bytes, 0, len);
                log.debug("返回数据区间:【{}-{}】", range, range + len);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                close(outputStream);
            }
        } else {
            try (OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = randomAccessFile.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void close(Closeable ...closeable) {
        if (StrUtils.isNotEmpty(closeable)) {
            for (Closeable c : closeable) {
                if (c != null) {
                    try {
                        c.close();
                    } catch (IOException e) {
                        // 这里可以记录日志或者进行其他处理，例如抛出RuntimeException或者不做任何处理
                        // throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
