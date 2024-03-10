package com.undraw.domain.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author readpage
 * @date 2023-02-18 13:51
 * @description 员工信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ContentStyle(verticalAlignment = VerticalAlignmentEnum.CENTER, horizontalAlignment = HorizontalAlignmentEnum.CENTER) //内容样式居中
@ApiModel(description = "员工信息")
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("工号")
    @ExcelProperty("工号")
    private int id;

    @ApiModelProperty("姓名")
    @ExcelProperty("姓名")
    private String name;

    @ApiModelProperty("性别")
    @ExcelProperty("性别")
    private String sex;

    @ApiModelProperty("年龄")
    @ExcelProperty("年龄")
    private int age;

    @ApiModelProperty("工资")
    @ExcelProperty("工资")
    private double salary;

    @ApiModelProperty("创建日期")
    @ExcelProperty("创建日期")
    @ColumnWidth(16)
    private LocalDateTime createTime = LocalDateTime.now();

    public Employee(int id, String name, String sex, int age, double salary) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.salary = salary;
    }

    public final static List<Employee> employeeList = new ArrayList<>(Arrays.asList(

            new Employee(1, "刘一", "男", 23, 6500),
            new Employee(2, "陈二", "男", 22, 7500),
            new Employee(3, "张三", "男", 25, 8500),
            new Employee(4, "李四", "男", 21, 8000),
            new Employee(5, "王五", "女", 24, 8000),
            new Employee(6, "赵六", "男", 23, 7800),
            new Employee(7, "孙七", "女", 24, 8000),
            new Employee(8, "周八", "男", 26, 8700),
            new Employee(9, "吴九", "女", 22, 8000),
            new Employee(10, "郑十", "男", 24, 9300)
    ));

    public int getAge() {
        this.age = 1;
        return age;
    }
}
