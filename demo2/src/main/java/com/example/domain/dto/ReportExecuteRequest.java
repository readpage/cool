package com.example.domain.dto;

import com.example.domain.model.ParamDef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 报告即时执行请求 — 不保存直接执行 SQL，所有参数通过 POST body 传递
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportExecuteRequest {
    /** SQL 模板 */
    private String sqlTemplate;
    /** 参数定义列表 */
    private List<ParamDef> parameters;
    /** 用户填写的参数值 */
    private Map<String, Object> params;
    /** 页码（从1开始） */
    private int current = 1;
    /** 每页大小 */
    private int size = 20;
}
