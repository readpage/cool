package com.undraw.basic;

import com.undraw.util.ImageUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author lnj
 * createTime 2018-10-19 15:39
 **/
public class ImageUtilTest {

    private static String userDir = System.getProperty("user.dir");

    /**
     * 生成缩略图
     */
    @Test
    public void testGenerateThumbnail2Directory() throws IOException {
        String path = userDir + "/upload/image/test.jpg";
        ImageUtils.compressSize(path, 5000);
    }
}