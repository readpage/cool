package com.undraw;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.baomidou.mybatisplus.generator.fill.Property;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CodeGenerator {

    private static final String URL = "jdbc:mysql://localhost:3306/cool?serverTimezone=GMT%2B8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    private static final String PROJECT = "cool-core-test";
    private static final String ProjectPath = System.getProperty("user.dir") + "/" + PROJECT;

    public static void main(String[] args) {
        FastAutoGenerator.create(URL, USERNAME, PASSWORD)
                // 全局配置
                .globalConfig(builder ->
                    // 设置作者
                    builder.author("readpage")
//                            .fileOverride() // 覆盖已生成文件
                            .disableOpenDir() //禁止打开输出目录
                            .commentDate("yyyy-MM-dd HH:mm") // 注释日期
                            .outputDir(ProjectPath+ "/src/main/java") //指定输出目录
                            .enableSwagger() // 开启 swagger 模式
                )
                // 包配置
                .packageConfig(builder ->
                    builder.parent("com.undraw") //父包名
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, ProjectPath+ "/src/main/resources/mapper"))  // 设置mapperXml生成路径
                )
                // 策略配置
                .strategyConfig((scanner, builder) -> builder.addInclude(getTables(scanner.apply("请输入表名,多个英文逗号分隔如:table,table2 ;所有输入 all")))
                        .controllerBuilder()
                        .enableRestStyle() //开启生成@RestController 控制器
                        .enableHyphenStyle() //开启驼峰转连字符

                        .entityBuilder()
                        .enableLombok()
                        .addTableFills(new Column("create_time", FieldFill.INSERT))
                        .addTableFills(new Property("updateTime", FieldFill.INSERT_UPDATE))

                        .serviceBuilder()
                        .formatServiceFileName("%sService") //格式化 service 接口文件名称
                        .build()
                )
                .execute();
    }

    // 处理 all 情况
    protected static List<String> getTables(String tables) {
        return "all".equals(tables) ? Collections.emptyList() : Arrays.asList(tables.split(","));
    }

}
