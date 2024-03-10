package com.undraw;

import cn.undraw.util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author readpage
 * @date 2023-02-08 15:04
 */
public class FileTest {

    // https://www.yc00.com/web/1701570280a1119247.html

    private static String userDir = System.getProperty("user.dir");
    @Test
    public void path() {
        String fileDir = userDir + "/file";
        System.out.println(fileDir);
    }
    /**
     * 创建文件，如果文件存在则文件名追加序号
     */
    @Test
    public void test() throws IOException {
        String fileDir = userDir + "/file/avatar.gif";
        System.out.println(FileUtils.createFile(fileDir));
        System.out.println(FileUtils.createSnowFile(fileDir));
    }

    @Test
    public void test3() throws IOException {
        File file = new File(userDir + "/file/hello.txt");
        File file2 = new File(userDir + "/file/hello2.txt");
        FileUtils.copyFile(file, file2);
    }

    @Test
    public void test4() {
        String fileDir = userDir + "/file.test";
        File file = new File(fileDir);
        System.out.println(file.getPath());
        System.out.println(file.getParentFile());

    }

    @Test
    public void test5() {
        System.out.println(FileUtils.format("/upload/hello/"));
        System.out.println(FileUtils.format("upload/hello/"));
    }
}
