package com.atguigu.srb.core.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class LendNoUtil {

    public static String getNo() {

        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String strDate = dtf.format(time);

        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            result.append(random.nextInt(10));
        }
        // 时间戳 + 随机的三位数
        return strDate + result;
    }

    /**
     * 生成标的编号
     *
     * @return 编号
     */
    public static String getLendNo() {

        return "LEND" + getNo();
    }

    /**
     * 投资人的投资编号
     *
     * @return 编号
     */
    public static String getLendItemNo() {

        return "INVEST" + getNo();
    }

    /**
     * 管理员放款的编号
     *
     * @return 编号
     */
    public static String getLoanNo() {

        return "LOAN" + getNo();
    }

    /**
     * 还款编号
     *
     * @return 编号
     */
    public static String getReturnNo() {
        return "RETURN" + getNo();
    }

    /**
     * 提现编号
     *
     * @return 编号
     */
    public static Object getWithdrawNo() {
        return "WITHDRAW" + getNo();
    }

    /**
     * 还款时的分期编号
     *
     * @return 编号
     */
    public static String getReturnItemNo() {
        return "RETURNITEM" + getNo();
    }

    /**
     * 充值编号
     *
     * @return 编号
     */
    public static String getChargeNo() {

        return "CHARGE" + getNo();
    }

    /**
     * 交易编号
     *
     * @return 编号
     */
    public static String getTransNo() {
        return "TRANS" + getNo();
    }

}