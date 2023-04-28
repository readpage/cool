package cn.undraw.util;

import cn.undraw.handler.exception.customer.CustomerException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDate;

/**
 * @author readpage
 * @date 2022-10-25 18:53
 */
public class FileUtils extends org.apache.commons.io.FileUtils {


    public static String upload(String fileDir, MultipartFile file) {
        return upload(fileDir, file,"");
    }

    public static String upload(String fileDir, MultipartFile file, String prefix) {
        MultipartFile[] files = new MultipartFile[]{file};
        return upload(fileDir, files, prefix);
    }

    public static String upload(String fileDir, MultipartFile[] files) {
        LocalDate now = LocalDate.now();
        String prefix = "/upload/" + now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth();
        return upload(fileDir, files, prefix);
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
            File filePath = createFile(fileDir, filter(originalFilename));
            //相对路径
            String relativePath = prefix + "/" + filePath.getName();
            try {
                files[i].transferTo(filePath);
            } catch (IOException e) {
                throw new CustomerException("上传文件失败", e);
            }
            if (i == 0) {
                dest.append(relativePath);
            } else {
                dest.append("||" + relativePath);
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

    /**
     * 获取不重复的文件对象,如果文件存在则文件名追加序号
     * @param fileDir
     * @param fileName
     * @param num
     * @return java.io.File
     */
    private static File getFile(String fileDir, String fileName, int num) {
        File file;
        String name = fileName.substring(0, fileName.lastIndexOf(".")).trim().replace(" ", "-"); //文件名 avatar
        String suffix = fileName.substring(fileName.lastIndexOf(".")); //后缀 如.png
        if (num == 0) {
            file = new File(fileDir + File.separator + name + suffix);
        } else {
            file = new File(fileDir + File.separator + name + "(" + num + ")" + suffix);
        }
        if (file.isFile()) {
            file = getFile(fileDir, fileName, ++num);
        }
        return file;
    }

    /**
     * 创建不重复的文件，如果文件存在则文件名追加序号
     * @param fileDir
     * @param fileName
     * @return java.io.File
     */
    public static File createFile(String fileDir, String fileName) {
        if (StrUtils.isEmpty(fileDir) || StrUtils.isEmpty(fileName)) return new File("");
        File file = getFile(fileDir, fileName, 0);
        try {
            FileUtils.touch(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public static void download(String filePath, HttpServletResponse response) {
        File file = new File(filePath);
        // 文件是否存在
        if (!file.exists()) {
            throw new CustomerException(file.getName() + "文件不存在!");
        }
        String suffix = filePath.substring(filePath.lastIndexOf(".")); //后缀 如.png
        //获取文件名
        String fileName = file.getName();
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