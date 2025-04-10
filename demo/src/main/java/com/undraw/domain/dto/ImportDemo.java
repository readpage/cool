package com.undraw.domain.dto;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author readpage
 * @date 2023-07-11 16:04
 */
@Data
public class ImportDemo {
    private String username;

    @ExcelProperty(index = 10)
    private String password;

    @ExcelProperty(index = 1)
    private String password2;

    @ExcelProperty(value = "用户名")
    private String username2;

    public String getKey() {
        return username;
    }
}
