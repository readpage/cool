package com.example.domain.dto;

import cn.undraw.util.result.R;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 批量导入结果
 */
public record ImportBatchResult(int total, int success, int fail, List<String> errors) {

    public static ImportBatchResult of(int total, int success, int fail, List<String> errors) {
        return new ImportBatchResult(total, success, fail, errors != null ? errors : List.of());
    }

    public static ImportBatchResult empty() {
        return new ImportBatchResult(0, 0, 0, List.of());
    }

    /** 生成摘要消息 */
    public String toMsg() {
        return String.format("导入完成：成功 %d 条，失败 %d 条", success, fail);
    }

    /** 构建响应数据 Map */
    public Map<String, Object> toDataMap() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", total);
        data.put("success", success);
        data.put("fail", fail);
        data.put("errors", errors);
        return data;
    }

    /** 快捷构建 R 响应 */
    public R toR() {
        return R.ok(toMsg(), toDataMap());
    }
}
