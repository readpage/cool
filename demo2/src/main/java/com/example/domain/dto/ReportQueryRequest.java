package com.example.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 报告查询请求 — 所有参数均通过 POST body 传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportQueryRequest {
    /** 用户填写的参数值 */
    private Map<String, Object> params;
    /** 页码（从1开始） */
    private int current = 1;
    /** 每页大小 */
    private int size = 20;
}
