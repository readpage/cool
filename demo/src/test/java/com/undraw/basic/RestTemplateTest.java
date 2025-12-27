package com.undraw.basic;

import cn.undraw.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

/**
 * @author readpage
 * @date 2023-02-02 17:00
 */
@SpringBootTest
public class RestTemplateTest {


    @Test
    public void test() {
        File file = FileUtils.createFile(System.getProperty("user.dir") + File.separator + "test.txt");
        System.out.println(file);
        file.delete();

    }
}
