package com.atguigu.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author coderxdh
 * @create 2022-04-27 11:23
 */
@AllArgsConstructor
@Getter
public enum LendStatusEnum {

    CHECK(0, "待发布"),
    INVEST_RUN(1, "募资中"),
    PAY_RUN(2, "还款中"),
    PAY_OK(3, "已结清"),
    FINISH(4, "结标"),
    CANCEL(-1, "已撤标"),
    ;

    private Integer status;
    private String msg;

    public static String getMsgByStatus(int status) {
        LendStatusEnum[] lendStatusEnums = LendStatusEnum.values();
        for (LendStatusEnum lendStatusEnum : lendStatusEnums) {
            if (lendStatusEnum.getStatus().equals(status)) {
                return lendStatusEnum.getMsg();
            }
        }
        return "";
    }
}
