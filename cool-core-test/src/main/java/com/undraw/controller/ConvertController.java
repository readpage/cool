package com.undraw.controller;

import cn.undraw.util.result.R;
import com.undraw.model.Fruit;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * @author readpage
 * @date 2023-05-06 17:13
 */
@RestController
@Api(tags = "转换处理")
@RequestMapping("/convert")
public class ConvertController {

    @GetMapping("/decimal")
    public R<Fruit> decimal() {
        Fruit fruit = new Fruit();
        BigDecimal decimal = new BigDecimal("2.2225");
        fruit.setPrice(decimal);
        fruit.setScore(480535617.050000);
        System.out.println(fruit);
        return R.ok(fruit);
    }
}
