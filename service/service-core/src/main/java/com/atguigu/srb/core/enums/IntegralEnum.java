package com.atguigu.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author coderxdh
 * @create 2022-04-25 10:54
 */
@AllArgsConstructor
@Getter
public enum IntegralEnum {

    BORROWER_ID_CARD(30, "身份信息积分"),
    BORROWER_CAR(60, "车辆信息积分"),
    BORROWER_HOUSE(100, "房产信息积分")
    ;

    private Integer integral;
    private String msg;
}
