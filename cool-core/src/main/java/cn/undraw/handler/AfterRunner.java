package cn.undraw.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Order(value = 1)
@Slf4j
public class AfterRunner implements ApplicationRunner {
    @Resource
    private Environment env;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        String name = env.getProperty("spring.application.name");
        String port = env.getProperty("server.port");
        String active = env.getProperty("spring.profiles.active");
        log.info("---[{}]---启动完成，当前使用的端口:[{}]，环境变量:[{}]---", name, port, active);
    }
}