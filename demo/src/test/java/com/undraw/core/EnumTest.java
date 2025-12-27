package com.undraw.core;

import org.junit.jupiter.api.Test;

public class EnumTest {
    enum TypeEnum {
        A, B, C;
    }

    @Test
    public void test() {
        System.out.println(TypeEnum.A.name());
        System.out.println("A".equals(TypeEnum.A.name()));
    }
}
