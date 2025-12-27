package com.undraw.domain.model;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @Schema(title = "ID")
    @ExcelProperty("ID")
    private Integer id;

    @Schema(title = "姓名")
    @ExcelProperty("姓名")
    private String name;

    @Schema(title = "年龄")
    @ExcelProperty("年龄")
    public int age;

    @Schema(title = "性别")
    @ExcelProperty("性别")
    private int sex;

    public final static List<Student> studentList = new ArrayList<>(Arrays.asList(
            new Student(1, "李明", 22, 0),
            new Student(2, "小红", 18, 1),
            new Student(3, "赵刚", 17, 0),
            new Student(4, "赵强", 16, 0),
            new Student(5, "小花", 15, 1),
            new Student(6, null, 17, 0)
    ));
}