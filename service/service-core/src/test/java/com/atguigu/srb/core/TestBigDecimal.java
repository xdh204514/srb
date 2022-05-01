package com.atguigu.srb.core;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author coderxdh
 * @create 2022-05-01 10:56
 */
public class TestBigDecimal {

    @Test
    public void test() {
        BigDecimal bigDecimal1 = new BigDecimal("1067.40362849");
        BigDecimal bigDecimal2= new BigDecimal("3202.21088549");
        BigDecimal bigDecimal3 = new BigDecimal("533.70181424");
        BigDecimal bigDecimal4 = new BigDecimal("320.22108854");
        BigDecimal x = bigDecimal1.add(bigDecimal2).add(bigDecimal3).add(bigDecimal4);
        System.out.println(x);
    }
}
