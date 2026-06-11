package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.entity.Option;
import com.example.service.OptionService;
import com.example.service.ReportOptionService;
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

    /** @ 前缀表示选项数据来自报告 SQL 执行，而非 options 表 */
    private static final String RPT_PREFIX = "@";

    @Resource
    private OptionService optionService;

    @Resource
    private ReportOptionService reportOptionService;

    @Operation(summary = "远程搜索选项 — type 以 '@' 开头时走报告SQL，否则查 options 表")
    @GetMapping("/search")
    public R<List<Option>> search(
            @Parameter(description = "选项类型（@前缀→报告SQL，否则→options表）") @RequestParam(required = false) String type,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "返回上限，默认 20") @RequestParam(defaultValue = "20") Integer limit) {
        // 分支：@ 前缀 → 报告 SQL 路径
        if (type != null && type.startsWith(RPT_PREFIX)) {
            String tableKey = type.substring(RPT_PREFIX.length());
            return R.ok(reportOptionService.getOptionsFromReport(tableKey, keyword, limit));
        }
        return R.ok(optionService.search(type, keyword, limit));
    }

    @Operation(summary = "根据 type 查询所有有效选项 — type 以 '@' 开头时走报告SQL，否则查 options 表")
    @GetMapping("/list")
    public R<List<Option>> listByType(
            @Parameter(description = "选项类型（@前缀→报告SQL，否则→options表）") @RequestParam(required = false) String type,
            @Parameter(description = "返回上限，默认 50") @RequestParam(defaultValue = "50") Integer limit) {
        // 分支：@ 前缀 → 报告 SQL 路径
        if (type != null && type.startsWith(RPT_PREFIX)) {
            String tableKey = type.substring(RPT_PREFIX.length());
            return R.ok(reportOptionService.getOptionsFromReport(tableKey, null, limit));
        }
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
