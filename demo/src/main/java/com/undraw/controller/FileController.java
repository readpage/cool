package com.undraw.controller;

import cn.undraw.util.FileUtils;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.util.io.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

import static cn.undraw.util.log.enums.OperateTypeEnum.DOWNLOAD;
import static cn.undraw.util.log.enums.OperateTypeEnum.UPLOAD;

/**
 * @author readpage
 * @date 2023-02-08 16:50
 */
@RestController
@Tag(name = "文件管理")
@RequestMapping("/file")
public class FileController {

    @Value("${file.upload}")
    private String filePath;

    @Value("${file.visit}")
    private String visit;

    @Operation(summary = "上传文件")
    @OperateLog(type = UPLOAD)
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public R<?> upload(@RequestBody MultipartFile file) {
        LocalDate now = LocalDate.now();
        String prefix = "/basic/" + now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth();
        String upload = visit + FileUtils.upload(filePath, file, prefix);
        return R.ok((Object) upload);
    }

    @Resource
    private FileUtil fileUtil;

    @Operation(summary = "下载文件")
    @OperateLog(type = DOWNLOAD)
    @GetMapping("/download")
    public void download(String objectName, HttpServletRequest request, HttpServletResponse response) {
        fileUtil.download(objectName, request, response);
    }

    @GetMapping("/getImage")
    public void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Thumbnails.of(System.getProperty("user.dir") + "/cool-core-test/upload/image/test.jpg").
                scale(0.5f).
                outputFormat("jpg").toOutputStream(response.getOutputStream());
    }
}

