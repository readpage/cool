package com.undraw;

import cn.undraw.annotation.EnableCool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author readpage
 * @date 2022-12-08 8:23
 */

@SpringBootApplication
@EnableCool
public class CoolCoreTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoolCoreTestApplication.class, args);
    }
}