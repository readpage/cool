package com.undraw.basic;

import cn.undraw.util.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author readpage
 * @date 2023-11-21 10:11
 */
public class FileUtilsTest {

    private String FileDir = "D:\\code\\Java\\cool\\cool-core-test\\upload";

    /**
     * readFileToString: 将文件内容读取为字符串。
     * @return void
     */
    @Test
    public void readFileToString() {
        File file = new File(FileDir + File.separator + "test.txt");
        String content = null;
        try {
            content = FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(content);
    }


    /**
     * writeStringToFile: 将字符串写入文件。如果文件已经存在，它将被覆盖。
     * @return void
     */
    @Test
    public void writeStringToFile() {
        File file = new File(FileDir + File.separator + "test.txt");
        String content = "Hello, World!";
        try {
            FileUtils.writeStringToFile(file, content, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 
     * copyFile: 复制文件。
     * @return void
     */
    @Test
    public void copyFile() throws IOException {
        File source = new File(FileDir + File.separator + "test.txt");
        File destination = new File(FileDir + File.separator + "test2.txt");
        FileUtils.copyFile(source, destination);
    }

    /** 
     * copyDirectory: 复制目录及其所有内容。
     * @return void
     */
    @Test
    public void copyDirectory() {
        File sourceDir = new File(FileDir + File.separator + "test");
        File destDir = new File(FileDir + File.separator + "test2");
        try {
            FileUtils.copyDirectory(sourceDir, destDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /** 
     * deleteQuietly: 删除文件或目录，而不抛出异常。如果文件或目录不存在，它不会执行任何操作。
     * @return void
     */
    @Test
    public void deleteQuietly() {
        File file = new File(FileDir + File.separator + "test2");
        FileUtils.deleteQuietly(file);
    }

    /** 
     * listFiles: 返回目录中匹配指定扩展名的所有文件的列表。
     * @return void
     */
    @Test
    public void listFiles() {
        // 第三个参数 recursive 是否递归
        Collection<File> files = FileUtils.listFiles(new File(FileDir), new String[]{"txt"}, true);
        for (File file : files) {
            System.out.println("Found file: " + file.getAbsolutePath());
        }
    }
}
