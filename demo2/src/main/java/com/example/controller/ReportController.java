package com.example.controller;

import cn.undraw.util.result.R;
import com.example.domain.dto.ReportExecuteRequest;
import com.example.domain.dto.ReportQueryRequest;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.model.ReportDefinition;
import com.example.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告管理 — Metabase 风格：SQL 模板 + 动态参数 + 自动列标题
 *
 * <h3>核心流程</h3>
 * <ol>
 *   <li>设计报告：POST /report/save（保存 SQL 模板 + 参数定义）</li>
 *   <li>获取参数：GET /report/{reportId}（前端据此渲染参数输入表单）</li>
 *   <li>执行查询：POST /report/{reportId}/query（传入参数值，返回表格数据 + 列标题）</li>
 * </ol>
 */
@Tag(name = "报告管理")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    // ==================== CRUD ====================

    @Operation(summary = "报告列表")
    @GetMapping("/list")
    public R<List<ReportDefinition>> list() {
        return R.ok(reportService.list());
    }

    @Operation(summary = "获取报告定义（含参数定义 + SQL模板）")
    @GetMapping("/{reportId}")
    public R<ReportDefinition> get(
            @Parameter(description = "报告ID") @PathVariable String reportId) {
        ReportDefinition def = reportService.getById(reportId);
        if (def == null) return R.fail("报告不存在: " + reportId);
        return R.ok(def);
    }

    @Operation(summary = "保存报告定义（新增/更新）")
    @PostMapping("/save")
    public R<String> save(@RequestBody ReportDefinition def) {
        reportService.save(def);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除报告")
    @DeleteMapping("/remove/{reportId}")
    public R<String> remove(
            @Parameter(description = "报告ID") @PathVariable String reportId) {
        reportService.delete(reportId);
        return R.ok("删除成功");
    }

    // ==================== 查询执行 ====================

    @Operation(summary = "执行报告查询 — SQL模板渲染 + 自动列标题，所有参数通过 POST body 传递")
    @PostMapping("/{reportId}/query")
    public R<ReportQueryResult> query(
            @Parameter(description = "报告ID") @PathVariable String reportId,
            @RequestBody ReportQueryRequest request) {
        return R.ok(reportService.query(reportId, request.getParams(), request.getCurrent(), request.getSize()));
    }

    @Operation(summary = "即时执行 SQL — 不保存报告，直接渲染 + 查询，所有参数通过 POST body 传递")
    @PostMapping("/execute")
    public R<ReportQueryResult> execute(@RequestBody ReportExecuteRequest request) {
        ReportDefinition def = new ReportDefinition();
        def.setId("__temp__");
        def.setName("临时报告");
        def.setSqlTemplate(request.getSqlTemplate());
        def.setParameters(request.getParameters());
        return R.ok(reportService.execute(def, request.getParams(), request.getCurrent(), request.getSize()));
    }
}
