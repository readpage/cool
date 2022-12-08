package cn.undraw.util;

import cn.undraw.handler.CustomerException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author readpage
 * @date 2022-10-25 18:53
 */
public class FileUtils {

    private static String absolutePath = new File("").getAbsolutePath();
    public static String upload(MultipartFile[] files, String fileDir) {
        if (fileDir == null) {
            fileDir = "basic";
        } else {
            fileDir = fileDir.replace(" ", "");
        }
        if (StrUtils.isNotEmpty(fileDir) && fileDir.charAt(fileDir.length() - 1) != '/') {
            fileDir += "/";
        }
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd");
        fileDir = "/upload/" + fileDir + now.getYear() + "/" + dateTimeFormatter.format(now);

        if (StrUtils.isEmpty(files)) {
            throw new CustomerException("文件为空");
        }
        StringBuilder dest = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            //原始文件名
            String originalFilename = files[i].getOriginalFilename();
            if (files[i].isEmpty()) {
                throw new CustomerException(originalFilename + "文件为空!");
            }
            long uuid = Instant.now().toEpochMilli();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//后缀 如.png
            String fileName = originalFilename.substring(0, originalFilename.lastIndexOf(".")).replace(" ", "") + "-" + uuid + suffix;

            //处理首尾'/'字符
            if (fileDir.charAt(fileDir.length() - 1) == '/') {
                fileDir = fileDir.substring(0, fileDir.length() - 1);
            }
            if (fileDir.charAt(0) != '/') {
                fileDir = "/" + fileDir;
            }
            //存储路径
            String filePath = fileDir + "/" + fileName;
            File destFile = new File(absolutePath + filePath);
            File parFile = destFile.getParentFile();
            if (!parFile.exists()) {
                parFile.mkdirs();
            }
            //将临时文件转存到指定位置
            try {
                files[i].transferTo(destFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (i == 0) {
                dest.append(filePath);
            } else {
                dest.append(", " + filePath);
            }
        }
      return dest.toString();
    }

    public static String upload(MultipartFile file, String path) {
        MultipartFile[] files = new MultipartFile[]{file};
        return upload(files, path);
    }

    public static String upload(MultipartFile[] file) {
        return upload(file, "");
    }

    public static String upload(MultipartFile file) {
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd");
        MultipartFile[] files = new MultipartFile[]{file};
        return upload(files, "");
    }

    public static void download(String filePath, HttpServletResponse response) {
        File file = new File(absolutePath + filePath);
        // 文件是否存在
        if (!file.exists()) {
            throw new CustomerException(file.getName() + "文件不存在!");
        }
        String suffix = filePath.substring(filePath.lastIndexOf(".")); //后缀 如.png
        //获取文件名
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('-')) + suffix;
        // 设置响应
        try {
            response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setContentType("application/octet-stream;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            ServletOutputStream os = response.getOutputStream();

            FileInputStream fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bos = new BufferedOutputStream(os);
            byte[] bytes = new byte[1024];
            int len;
            while ((len = bis.read(bytes)) != -1) {
                bos.write(bytes, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
