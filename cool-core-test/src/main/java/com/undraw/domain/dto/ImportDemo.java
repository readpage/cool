package com.undraw.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author readpage
 * @date 2023-07-11 16:04
 */
@Data
public class ImportDemo {
    @ExcelProperty(index = 0)
    private String username;

    @ExcelProperty(index = 1)
    private String password;
}
