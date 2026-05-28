package com.example;

import cn.undraw.annotation.EnableCool;
import com.example.template.annotation.SqlScan;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCool
@SqlScan("com.example.dao")          // FilterParam 自定义 DAO
@MapperScan("com.example.mapper")    // MyBatis-Plus Mapper
public class Demo2Application {
    public static void main(String[] args) {
        SpringApplication.run(Demo2Application.class, args);
    }

}
