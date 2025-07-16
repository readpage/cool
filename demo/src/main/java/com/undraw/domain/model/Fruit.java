package com.undraw.domain.model;


import cn.undraw.util.decimal.annotation.BigDecimalFormat;
import cn.undraw.util.decimal.annotation.DecimalFormat;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author readpage
 * @date 2022-11-15 14:33
 */
@Getter
@Setter
@ToString
@BigDecimalFormat
@DecimalFormat
@AllArgsConstructor
@NoArgsConstructor
public class Fruit {
    private BigDecimal price;

    private BigDecimal price2;

    @BigDecimalFormat(access = BigDecimalFormat.Access.TenThousand)
    private BigDecimal price3;

    @DecimalFormat
    private Double score;

}
