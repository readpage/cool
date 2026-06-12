package com.example.domain.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 报表权限配置 DTO
 *
 * <pre>
 *   JSON 存储结构（存于 report.permission_config）：
 *   {
 *     "roleIds": [3, 5, 7]
 *   }
 *
 *   roleIds 为空 = 仅创建者可访问（白名单模式）
 * </pre>
 */
@Data
public class ReportPermissionDto {

    /** 允许访问的角色 ID 列表（白名单模式，为空 = 仅创建者可访问） */
    private List<Integer> roleIds = new ArrayList<>();
}
