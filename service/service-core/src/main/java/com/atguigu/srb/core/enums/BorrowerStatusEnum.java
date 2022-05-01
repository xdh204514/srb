package com.atguigu.srb.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author coderxdh
 * @create 2022-04-24 16:05
 */
@AllArgsConstructor
@Getter
public enum BorrowerStatusEnum {

    NO_AUTH(0, "未认证"),
    AUTH_RUN(1, "认证中"),
    AUTH_OK(2, "认证成功"),
    AUTH_FAIL(-1, "认证失败"),
    ;

    private Integer status;
    private String msg;

    public static String getMsgByStatus(int status) {
        BorrowerStatusEnum arrObj[] = BorrowerStatusEnum.values();
        for (BorrowerStatusEnum obj : arrObj) {
            if (status == obj.getStatus().intValue()) {
                return obj.getMsg();
            }
        }
        return "";
    }
}
