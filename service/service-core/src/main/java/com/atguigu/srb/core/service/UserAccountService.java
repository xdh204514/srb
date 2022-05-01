package com.atguigu.srb.core.service;

import com.atguigu.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
public interface UserAccountService extends IService<UserAccount> {

    String commitCharge(String chargeAmt, Long userId);

    String notify(Map<String, Object> paramMap);

    BigDecimal getAmount(Long userId);

    String commitWithdraw(BigDecimal fetchAmt, Long userId);

    String notifyWithdraw(Map<String, Object> paramMap);
}
