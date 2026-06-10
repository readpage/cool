package com.example.controller;

import cn.undraw.util.result.R;
import lombok.extern.slf4j.Slf4j;
import com.example.domain.dto.ReportParam;
import com.example.domain.dto.ReportQueryResult;
import com.example.domain.dto.ReportSaveRequest;
import com.example.domain.entity.Datasource;
import com.example.service.DatasourceService;
import com.example.service.ReportService;
import com.example.template.datasource.DynamicJdbcFactory;
import com.example.template.util.FilterParam;
import com.example.util.excel.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 报告管理接口
 */
@Slf4j
@Tag(name = "报告管理")
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    @Resource
    private DatasourceService datasourceService;

    @Resource
    private DynamicJdbcFactory dynamicJdbcFactory;

    /** 默认用户 ID（后续接入认证后替换） */
    private static final Long DEFAULT_USER_ID = 1L;

    @Operation(summary = "报告列表")
    @GetMapping("/list")
    public R<List<ReportSaveRequest>> list() {
        return R.ok(reportService.list());
    }

    @Operation(summary = "管理端获取报告定义 — 纯 sys_config，无 copy-on-read")
    @GetMapping("/get")
    public R<ReportSaveRequest> getForEdit(@RequestParam String tableKey) {
        return R.ok(reportService.getByTableKey(tableKey));
    }

    @Operation(summary = "用户端获取报告定义 — copy-on-read，user_config 优先")
    @GetMapping("/user/get")
    public R<ReportSaveRequest> getForUser(@RequestParam String tableKey) {
        return R.ok(reportService.getByTableKey(tableKey, DEFAULT_USER_ID));
    }

    @Operation(summary = "保存报告（新增/更新），含 report 实体 + displayConfig")
    @PostMapping("/save")
    public R save(@RequestBody ReportSaveRequest req) {
        reportService.save(req);
        return R.ok((Object) req.getReport().getTableKey());
    }

    @Operation(summary = "删除报告")
    @DeleteMapping("/remove/{tableKey}")
    public R<String> remove(@PathVariable String tableKey) {
        reportService.deleteByTableKey(tableKey);
        return R.ok("删除成功");
    }

    @Operation(summary = "管理端执行报告查询（预览）")
    @PostMapping("/{tableKey}/query")
    public R<ReportQueryResult> query(@PathVariable String tableKey, @RequestBody FilterParam param) {
        log.info("==> report/query tableKey:{}, filter:{}, sort:{}, page:{}/{}",
                tableKey, param.getFilter(), param.getSort(), param.getCurrent(), param.getSize());
        return R.ok(reportService.queryByTableKey(tableKey, param));
    }

    @Operation(summary = "用户端执行报告查询")
    @PostMapping("/user/query")
    public R<ReportQueryResult> queryForUser(@RequestParam String tableKey, @RequestBody FilterParam param) {
        log.info("==> report/user/query tableKey:{}, filter:{}, sort:{}, page:{}/{}",
                tableKey, param.getFilter(), param.getSort(), param.getCurrent(), param.getSize());
        return R.ok(reportService.queryByTableKey(tableKey, param));
    }

    @Operation(summary = "即时执行 SQL")
    @PostMapping("/execute")
    public R<ReportQueryResult> execute(@RequestBody ReportParam param) {
        log.info("==> report/execute sql:{}, filter:{}, sort:{}, page:{}/{}",
                param.getSqlTemplate(), param.getFilter(), param.getSort(),
                param.getCurrent(), param.getSize());
        return R.ok(reportService.execute(param.getSqlTemplate(), param));
    }

    @Operation(summary = "导出已保存报告 Excel")
    @PostMapping("/{tableKey}/export")
    public void export(@PathVariable String tableKey, @RequestBody FilterParam param,
                       HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = reportService.queryAllByTableKey(tableKey, param);

        // 根据 tableKey 获取报告名作为导出文件名
        String fileName = "导出数据";
        try {
            ReportSaveRequest reportDef = reportService.getByTableKey(tableKey);
            if (reportDef != null && reportDef.getReport() != null
                    && reportDef.getReport().getName() != null
                    && !reportDef.getReport().getName().isBlank()) {
                fileName = reportDef.getReport().getName();
            }
        } catch (Exception e) {
            log.warn("获取报告名失败，使用默认文件名: tableKey={}", tableKey, e);
        }

        ExcelUtils.exportForMap(response, fileName, param, data);
    }

    @Operation(summary = "即时执行 SQL 并导出 Excel")
    @PostMapping("/execute/export")
    public void executeAndExport(@RequestBody ReportParam param,
                                  HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = reportService.executeForExport(param.getSqlTemplate(), param);
        ExcelUtils.exportForMap(response, "导出数据", param, data);
    }

    // ==================== 数据源管理 ====================

    @Operation(summary = "数据源列表")
    @GetMapping("/datasource/list")
    public R<List<Datasource>> datasourceList() {
        return R.ok(datasourceService.list());
    }

    @Operation(summary = "新增/编辑数据源")
    @PostMapping("/datasource/save")
    public R<Long> datasourceSave(@RequestBody Datasource ds) {
        datasourceService.save(ds);
        // 重新从 DB 查询完整实体（含密码），然后用最新配置刷新连接池
        Datasource full = datasourceService.getById(ds.getId());
        if (full != null) {
            dynamicJdbcFactory.refresh(full);
        }
        return R.ok(ds.getId());
    }

    @Operation(summary = "删除数据源")
    @DeleteMapping("/datasource/remove/{id}")
    public R<String> datasourceRemove(@PathVariable Long id) {
        // 检查是否有报告引用此数据源
        long refCount = reportService.countByDatasourceId(id);
        if (refCount > 0) {
            return R.fail("该数据源被 " + refCount + " 个报告引用，请先解除关联后再删除");
        }
        datasourceService.deleteById(id);
        dynamicJdbcFactory.evict(id);
        return R.ok("删除成功");
    }

    @Operation(summary = "测试数据源连接")
    @PostMapping("/datasource/test")
    public R datasourceTestConnect(@RequestBody Datasource ds) {
        String error = datasourceService.testConnect(ds);
        if (error == null) {
            return R.ok("连接成功");
        }
        return R.fail(error);
    }
}
