package com.undraw.controller;

import cn.undraw.util.FileUtils;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

import static cn.undraw.util.log.enums.OperateTypeEnum.DOWNLOAD;
import static cn.undraw.util.log.enums.OperateTypeEnum.UPLOAD;

/**
 * @author readpage
 * @date 2023-02-08 16:50
 */
@RestController
@Api(tags = "文件管理")
@RequestMapping("/file")
public class FileController {

    @Value("${server.upload}")
    private String fileDir;

    @ApiOperation("上传文件")
    @OperateLog(type = UPLOAD)
    @PostMapping("/upload")
    public R<?> upload(MultipartFile[] files) {
        LocalDate now = LocalDate.now();
        String prefix = "/upload/" + now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth();
        String upload = FileUtils.upload(fileDir, files, prefix);
        return R.ok((Object) upload);
    }

    @ApiOperation("下载文件")
    @OperateLog(type = DOWNLOAD)
    @GetMapping("/download")
    public void download(String filePath, HttpServletResponse response) {
        FileUtils.download(fileDir + filePath, response);
    }

    @GetMapping("/getImage")
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Thumbnails.of(System.getProperty("user.dir") + "/cool-core-test/upload/image/test.jpg").
                scale(0.5f).
                outputFormat("jpg").toOutputStream(response.getOutputStream());
    }
}

