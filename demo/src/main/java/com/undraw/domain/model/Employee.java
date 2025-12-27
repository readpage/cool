package com.undraw.domain.model;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ContentStyle;
import cn.idev.excel.enums.poi.HorizontalAlignmentEnum;
import cn.idev.excel.enums.poi.VerticalAlignmentEnum;
import cn.undraw.util.decimal.annotation.DecimalFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author readpage
 * @date 2023-02-18 13:51
 * @description 员工信息
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@ContentStyle(verticalAlignment = VerticalAlignmentEnum.CENTER, horizontalAlignment = HorizontalAlignmentEnum.CENTER) //内容样式居中
@Schema(title = "员工信息")
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "工号")
    @ExcelProperty("工号")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(title = "姓名")
    @ExcelProperty("姓名")
    private String name;

    @Schema(title = "性别")
    @ExcelProperty("性别")
    private String sex;

    @Schema(title = "年龄")
    @ExcelProperty("年龄")
    private int age;

    @Schema(title = "工资")
    @ExcelProperty("工资")
    @DecimalFormat
    private Double salary;

    @Schema(title = "创建日期")
    @ExcelProperty(value = "创建日期")
    private LocalDate crateDate = LocalDate.now();

    @Schema(title = "创建时间")
    @ExcelProperty("创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;

    public Employee(Long id, String name, String sex, int age, double salary) {
        this.id = id;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.salary = salary;
    }

    public final static List<Employee> employeeList = new ArrayList<>(Arrays.asList(
            new Employee(1L, "刘一", "男", 23, 6500),
            new Employee(2L, "陈二", "男", 22, 7500),
            new Employee(3L, "张三", "男", 25, 8500),
            new Employee(4L, "李四", "男", 22, 8000),
            new Employee(5L, "王五", "女", 24, 8000),
            new Employee(6L, "赵六", "男", 23, 7800),
            new Employee(7L, "孙七", "女", 24, 8000),
            new Employee(8L, "周八", "男", 26, 8700),
            new Employee(9L, "吴九", "女", 22, 8000),
            new Employee(10L, "郑十", "男", 24, 9300)
    ));

    private void hello() {
        System.out.println(String.format("hello %s!", this.name));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;

        return id != null ? id.equals(employee.id) : employee.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
