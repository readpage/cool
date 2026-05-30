package com.example.service;

import java.util.ArrayList;
import java.util.List;

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
}
