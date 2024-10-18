package com.undraw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class SpringDocConfig {
    /**
     * 访问地址: http://localhost:8081/swagger-ui/index.html
     * 构建Swagger3.0文档说明
     * @return 返回 OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {

        // 联系人信息(contact)，构建API的联系人信息，用于描述API开发者的联系信息，包括名称、URL、邮箱等
        // name：文档的发布者名称 url：文档发布者的网站地址，一般为企业网站 email：文档发布者的电子邮箱
        Contact contact = new Contact()
                .name("readpage")                             // 作者名称
                .email("readpage@qq.com")                   // 作者邮箱
                .url("https://gitee.com/f1dao")  // 介绍作者的URL地址
                .extensions(new HashMap<String, Object>()); // 使用Map配置信息（如key为"name","email","url"）

        //创建Api帮助文档的描述信息、联系人信息(contact)、授权许可信息(license)
        Info info = new Info()
                .title("Swagger3接口文档")      // Api接口文档标题（必填）
                .description("即使再小的帆也能远航")     // Api接口文档描述
                .termsOfService("https://readpage.cn/")
                .version("1.0.0")                                  // Api接口版本
                .contact(contact);
        // 返回信息
        return new OpenAPI()
                .info(info);       // 配置Swagger3.0描述信息
    }
}
