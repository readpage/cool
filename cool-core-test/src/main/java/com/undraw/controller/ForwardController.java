package com.undraw.controller;

import cn.undraw.util.RestTemplateUtil;
import cn.undraw.util.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author readpage
 * @date 2023-10-25 10:09
 */

@RestController
@RequestMapping("/forward")
@Tag(name = "转发处理")
public class ForwardController {

    @Resource
    private RestTemplateUtil restTemplateUtil;


    @Operation(summary = "导出文件")
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        String url = "http://localhost:7032/excel/export";
        restTemplateUtil.transferFile(url, response);
    }

    @Operation(summary = "导入文件")
    @PostMapping("/import")
    public R upload(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.MULTIPART_FORM_DATA));

        Map<String, Object> map = new HashMap<>();
        map.put("file", file);
        map.put("arg", "test");
        return restTemplateUtil.postR("http://localhost:7032/excel/import", headers, map);
    }

}
