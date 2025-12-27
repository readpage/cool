package com.undraw.controller;


import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static cn.undraw.util.log.enums.OperateTypeEnum.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
@RestController
@RequestMapping("/model")
@Tag(name = "通用")
public class ModelController {

    @Resource
    private ModelService modelService;

    @Operation(summary = "添加")
    @OperateLog(type = CREATE)
    @PostMapping("/{tableName}/save")
    public R save(@PathVariable("tableName") String tableName, @RequestBody Map map) {
        map.put("tableName", tableName);
        return R.ok(modelService.save(map));
    }

    @Operation(summary = "修改")
    @OperateLog(type = UPDATE)
    @PutMapping("/{tableName}/update")
    public R update(@PathVariable("tableName") String tableName, @RequestBody Map map) {
        map.put("tableName", tableName);
        return R.ok(modelService.update(map));
    }

    @Operation(summary = "查询")
    @GetMapping("/{tableName}/list")
    public R list(@PathVariable("tableName") String tableName, @RequestParam Map<String, Object> map) {
        map.put("tableName", tableName);
        return R.ok(modelService.list(map));
    }

    @Operation(summary = "分页查询")
    @GetMapping("/{tableName}/page")
    public R page(@PathVariable("tableName") String tableName, @RequestParam Map<String, Object> map) {
        map.put("tableName", tableName);
        return R.ok(modelService.page(map));
    }

    @Operation(summary = "导入excel")
    @PostMapping(value = "/{tableName}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R upload(@PathVariable("tableName") String tableName, @RequestBody MultipartFile file) {
        return R.ok(modelService.upload(tableName, file));
    }


    @GetMapping("/{tableName}/export")
    @Operation(summary = "导出excel")
    @OperateLog(type = EXPORT)
    public void export(@PathVariable("tableName") String tableName, HttpServletResponse response, @RequestParam Map<String, Object> map) {
        map.put("tableName", tableName);
        modelService.export(response, map);
    }
}

