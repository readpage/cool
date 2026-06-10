package com.example.domain.dto;

import com.example.domain.entity.SysConfig;
import com.example.domain.entity.UserConfig;
import lombok.Data;

/**
 * 配置合并查询结果 — 用于 GET /config/merged
 * <pre>
 *   后端聚合 user_config + sys_config + copy-on-read，
 *   前端一次请求拿到有效配置，无需自己 fallback。
 * </pre>
 */
@Data
public class ConfigResult {

    /** 配置 JSON 字符串 */
    private String configValue;

    /** 来源："user" 或 "system" */
    private String source;

    /** sys_config.version，user_config 时为 0 */
    private Integer version;

    public static ConfigResult from(UserConfig uc) {
        ConfigResult r = new ConfigResult();
        r.configValue = uc.getConfigValue();
        r.source = "user";
        r.version = 0;
        return r;
    }

    public static ConfigResult from(SysConfig sc) {
        ConfigResult r = new ConfigResult();
        r.configValue = sc.getConfigValue();
        r.source = "system";
        r.version = sc.getVersion() != null ? sc.getVersion() : 0;
        return r;
    }
}
