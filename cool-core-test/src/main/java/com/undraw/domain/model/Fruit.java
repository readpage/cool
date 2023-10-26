package com.undraw.domain.model;


import cn.undraw.util.decimal.annotation.BigDecimalFormat;

import java.math.BigDecimal;

/**
 * @author readpage
 * @date 2022-11-15 14:33
 */
@BigDecimalFormat
public class Fruit {
    private BigDecimal price;

    private BigDecimal price2;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice2() {
        return price2;
    }

    public void setPrice2(BigDecimal price2) {
        this.price2 = price2;
    }

    @Override
    public String toString() {
        return "Fruit{" +
                "price=" + price +
                ", price2=" + price2 +
                '}';
    }
}
