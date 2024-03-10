package com.undraw;

import cn.undraw.util.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

/**
 * @author readpage
 * @date 2023-02-02 17:00
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RestTemplateTest {


    @Test
    public void test() {
        File file = FileUtils.createSnowFile(System.getProperty("user.dir") + File.separator + "test.txt");
        System.out.println(file);
        file.delete();

    }
}
