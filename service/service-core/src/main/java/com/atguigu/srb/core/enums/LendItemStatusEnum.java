package com.atguigu.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author coderxdh
 * @create 2022-04-23 18:46
 */
@AllArgsConstructor
@Getter
public enum LendItemStatusEnum {

    DEFAULT(0, "默认"),
    PAID(1, "已支付"),
    REPAID(2, "已还款"),
    ;

    private Integer status;
    private String msg;
}
