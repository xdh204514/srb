package com.atguigu.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author coderxdh
 * @create 2022-04-28 10:59
 */
@AllArgsConstructor
@Getter
public enum TransTypeEnum {
    RECHARGE(1,"充值"),
    INVEST_LOCK(2,"投标锁定"),
    INVEST_UNLOCK(3,"放款解锁"),
    CANCEL_LEND(4,"撤标"),
    BORROW_BACK(5,"放款到账"),
    RETURN_DOWN(6,"还款扣减"),
    INVEST_BACK(7,"出借回款"),
    WITHDRAW(8,"提现"),
    ;

    private Integer transType;
    private String transTypeName;
}
