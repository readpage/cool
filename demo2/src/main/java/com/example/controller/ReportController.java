package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.dto.ReportParam;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.service.ReportService;
import com.example.template.util.FilterParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告管理接口
 */
@Tag(name = "报告管理")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    /** 默认用户 ID（后续接入认证后替换） */
    private static final Long DEFAULT_USER_ID = 1L;

    @Operation(summary = "报告列表")
    @GetMapping("/list")
    public R<List<ReportSaveRequest>> list() {
        return R.ok(reportService.list());
    }

    @Operation(summary = "获取报告定义（含用户展示配置，copy-on-read）")
    @GetMapping("/{tableKey}")
    public R<ReportSaveRequest> get(@PathVariable String tableKey) {
        return R.ok(reportService.getByTableKey(tableKey, DEFAULT_USER_ID));
    }

    @Operation(summary = "保存报告（新增/更新），含 report 实体 + displayConfig")
    @PostMapping("/save")
    public R<String> save(@RequestBody ReportSaveRequest req) {
        reportService.save(req);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除报告")
    @DeleteMapping("/remove/{tableKey}")
    public R<String> remove(@PathVariable String tableKey) {
        reportService.deleteByTableKey(tableKey);
        return R.ok("删除成功");
    }

    @Operation(summary = "执行报告查询")
    @PostMapping("/{tableKey}/query")
    public R<ReportQueryResult> query(@PathVariable String tableKey, @RequestBody FilterParam param) {
        return R.ok(reportService.queryByTableKey(tableKey, param));
    }

    @Operation(summary = "即时执行 SQL")
    @PostMapping("/execute")
    public R<ReportQueryResult> execute(@RequestBody ReportParam param) {
        return R.ok(reportService.execute(param.getSqlTemplate(), param));
    }
}
