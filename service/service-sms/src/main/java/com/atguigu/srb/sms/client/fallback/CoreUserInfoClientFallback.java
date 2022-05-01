package com.atguigu.srb.sms.client.fallback;

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.sms.client.CoreUserInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author coderxdh
 * @create 2022-04-22 20:25
 */

@Service
@Slf4j
public class CoreUserInfoClientFallback implements CoreUserInfoClient {
    @Override
    public R checkMobile(String mobile) {
        log.error("远程调用失败，服务熔断");
        log.info("当无法校验手机号是否已注册时，那么就统一认为没有注册过，直接发送短信");
        return R.ok().message("远程调用失败，服务熔断").data("isExist", false);
    }
}
