package com.undraw;

import cn.undraw.annotation.EnableCool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author readpage
 * @date 2022-12-08 8:23
 */

@SpringBootApplication
@EnableCool
@EnableAsync
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}