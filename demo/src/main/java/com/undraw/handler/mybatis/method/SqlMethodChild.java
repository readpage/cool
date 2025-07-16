package com.undraw.handler.mybatis.method;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SqlMethodChild {
    LIST_BY_key("listByKey", "根据key查询查询数据", "<script>SELECT * FROM table WHERE (%s) IN (%s)</script>");

    private final String method;
    private final String desc;
    private final String sql;
}
