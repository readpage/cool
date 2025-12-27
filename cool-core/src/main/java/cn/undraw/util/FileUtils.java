package cn.undraw.util;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.snowflake.SnowflakeUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * @author readpage
 * @date 2022-10-25 18:53
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

    /**
     *
     * @param fileDir
     * @param file
     * @param
     * @return java.lang.String 文件上传返回值相对路径 前缀 +文件名 如 /upload/2023/10/24/filename
     */
    public static String upload(String fileDir, MultipartFile file) {
        return upload(fileDir, file,"");
    }

    /**
     *
     * @param fileDir
     * @param file
     * @param prefix 自定义前缀 -> 文件上传返回值相对路径 前缀 +文件名 如 /2023/10/24/filename
     * @return java.lang.String
     */
    public static String upload(String fileDir, MultipartFile file, String prefix) {
        MultipartFile[] files = new MultipartFile[]{file};
        return upload(fileDir, files, prefix);
    }

    public static String getDatePrefix() {
        LocalDate now = LocalDate.now();
        return "/" + now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth();
    }

    /**
     *
     * @param fileDir
     * @param files
     * @param
     * @return java.lang.String 文件上传返回值相对路径 前缀 +文件名 如 /2023/10/24/filename,/2023/10/24/filename1
     */
    public static String upload(String fileDir, MultipartFile[] files) {
        return upload(fileDir, files, getDatePrefix());
    }

    public static String upload(String fileDir, MultipartFile[] files, String prefix) {
        if (StrUtils.isEmpty(files)) {
            throw new CustomerException("文件为空");
        }

        if (StrUtils.isEmpty(fileDir)) {
            throw new CustomerException("上传的地址不能为空");
        } else {
            fileDir = filter(fileDir);
        }

        fileDir += format(prefix);

        // 文件列表存储路径
        StringBuilder dest = new StringBuilder();
        for (int i = 0; i < files.length; i++) {
            //原始文件名
            String originalFilename = files[i].getOriginalFilename();
            //创建不重复的文件
            File filePath = createFile(fileDir, originalFilename);
            try {
                files[i].transferTo(filePath);
            } catch (IOException e) {
                throw new CustomerException("上传文件失败", e);
            }
            //文件上传返回值相对路径
            String relativePath = prefix + "/" + filePath.getName();
            if (i == 0) {
                dest.append(relativePath);
            } else {
                dest.append("," + relativePath);
            }
        }
        return dest.toString();
    }

    /**
     * 格式化路径 处理首尾'/'字符,首加'/'尾去'/'
     * @return java.lang.String
     */
    public static String format(String path) {
        if (StrUtils.isNotEmpty(path)) {
            // 过滤处理只保留英文，数字，中文，/-_.~\
            path = filter(path);
            if (path.charAt(0) != '/') {
                path = "/" + path.trim().replace(" ", "-");
            }
            if (path.charAt(path.length() - 1) == '/') {
                path = path.substring(0, path.length() - 1);
            }
        } else {
            path = "";
        }
        return path;
    }

    /**
     * 过滤处理，只保留英文，数字，中文，/-_.~:\
     * @param str
     * @return java.lang.String
     */
    public static String filter(String str) {
        return str.replaceAll("[^a-zA-Z0-9\\u4E00-\\u9FA5/\\-_.~:\\\\]", "");
    }



    private static File getFile(String filePath, int num) {
        File file;
        if (num == 0) {
            file = new File(filePath);
        } else {
            String prefix = filePath.substring(0, filePath.lastIndexOf(".")).trim().replace(" ", "-");
            String suffix = filePath.substring(filePath.lastIndexOf(".")); //后缀 如.png
            file = new File(prefix + "(" + num + ")" + suffix);
        }
        if (file.exists()) {
            file = getFile(filePath, ++num);
        }
        return file;
    }


    /**
     * 创建文件
     * @param filePath
     * @param nodup true: 文件名重复追加序号； false: 文件名为雪花序列号
     * @return java.io.File
     */
    public static File createFile(String filePath, boolean nodup) {
        if (StrUtils.isEmpty(filePath)) throw new RuntimeException("文件路径为空");
        File file;
        if (nodup) {
            file = getFile(filePath, 0);
        } else {
            file = new File(filePath);
        }
        try {
            FileUtils.touch(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * 创建文件
     * @param fileDir
     * @param fileName
     * @return java.io.File
     */
    public static File createFile(String fileDir, String fileName) {
        if (StrUtils.isEmpty(fileDir) || StrUtils.isEmpty(fileName)) return new File("");
        String suffix = fileName.substring(fileName.lastIndexOf(".")); //后缀 如.png
        fileName = SnowflakeUtils.nextId() + suffix;
        File file = new File(fileDir + File.separator + fileName);
        try {
            FileUtils.touch(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * 创建文件
     * @param filePath
     * @return java.io.File
     */
    public static File createFile(String filePath) {
        if (StrUtils.isEmpty(filePath)) throw new RuntimeException("文件路径为空");
        Path path = Paths.get(filePath);
        String parent = path.getParent().toString();
        String fileName = path.getFileName().toString();
        return createFile(parent, fileName);
    }

    public static void download(String filePath, HttpServletResponse response) {
        File file = new File(filePath);
        // 文件是否存在
        if (!file.exists()) {
            throw new CustomerException(file.getName() + "文件不存在!");
        }
        String suffix = filePath.substring(filePath.lastIndexOf(".")); //后缀 如.png
        //获取文件名
        String filename = file.getName();
        // 设置响应
        try {
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.addHeader("Content-Length", "" + file.length());
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