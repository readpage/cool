package com.undraw.model;

import com.undraw.service.SystemLogService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author readpage
 * @date 2022-11-15 14:33
 */
@Service
public class Fruit {
    private BigDecimal price;

    @Resource
    private SystemLogService systemLogService;

    public BigDecimal getPrice() {
        System.out.println(systemLogService);
        return price;
    }
}
