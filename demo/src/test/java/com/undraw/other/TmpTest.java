package com.undraw.other;

import org.junit.jupiter.api.Test;

import java.util.StringJoiner;

public class TmpTest {

    @Test
    public void test() {
        StringJoiner column = new StringJoiner(",", ",", ")");
        column.add("a");
        column.add("b");
        column.add("c");
        System.out.println(column.toString());
    }
}
