package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.entity.Option;
import com.example.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author readpage
 */
@Tag(name = "选项管理", description = "选项远程搜索等接口")
@RestController
@RequestMapping("/option")
public class OptionController {

    @Resource
    private OptionService optionService;

    @Operation(summary = "远程搜索选项 — 根据 type 和 keyword 模糊匹配 label，默认最多返回 20 条")
    @GetMapping("/search")
    public R<List<Option>> search(
            @Parameter(description = "选项类型") @RequestParam(required = false) String type,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "返回上限，默认 20") @RequestParam(defaultValue = "20") Integer limit) {
        return R.ok(optionService.search(type, keyword, limit));
    }

    @Operation(summary = "根据 type 查询所有有效选项，默认最多返回 50 条")
    @GetMapping("/list")
    public R<List<Option>> listByType(
            @Parameter(description = "选项类型") @RequestParam(required = false) String type,
            @Parameter(description = "返回上限，默认 50") @RequestParam(defaultValue = "50") Integer limit) {
        return R.ok(optionService.listByType(type, limit));
    }

    @Operation(summary = "保存选项（id=null 新增，id≠null 修改）")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody Option option) {
        return R.ok(optionService.saveOrUpdate(option));
    }

    @Operation(summary = "根据ID删除选项")
    @DeleteMapping("/remove")
    public R<Boolean> remove(@RequestBody List<Long> ids) {
        return R.ok(optionService.removeBatchByIds(ids));
    }
}
