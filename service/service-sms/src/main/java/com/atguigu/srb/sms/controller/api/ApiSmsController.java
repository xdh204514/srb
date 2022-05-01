package com.atguigu.srb.sms.controller.api;

import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.utils.RandomUtil;
import com.atguigu.srb.common.utils.RegexValidateUtil;
import com.atguigu.srb.sms.client.CoreUserInfoClient;
import com.atguigu.srb.sms.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author coderxdh
 * @create 2022-04-19 23:04
 */
@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理接口")
@Slf4j
public class ApiSmsController {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;
    
    @Resource
    private CoreUserInfoClient coreUserInfoClient;

    @ApiOperation("获取手机验证码")
    @GetMapping("/send/{mobile}")
    public R send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile){

        // MOBILE_NULL_ERROR(-202, "手机号不能为空"),
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        // MOBILE_ERROR(-203, "手机号不正确"),
        Assert.isTrue(RegexValidateUtil.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);

        // 优化：在发送验证码前就判断该用户是否已经注册过
        final R r = coreUserInfoClient.checkMobile(mobile);
        boolean checkResult = (boolean) r.getData().get("isExist");
        Assert.isTrue(!checkResult, ResponseEnum.MOBILE_EXIST_ERROR);

        // 生成验证码
        String code = RandomUtil.getSixBitRandom();
        // 组装短信模板参数
        Map<String,Object> param = new HashMap<>();
        param.put("code", code);
        // 发送短信
//        smsService.send(mobile, SmsProperties.LOGIN_TEMPLATE_CODE, param);

        // 将验证码存入Redis
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 5, TimeUnit.MINUTES);

        // 因为在 SmsService 已经处理过异常了，能够执行到这里就是没问题了，可以直接返回
        return R.ok().message("短信发送成功");
    }
}
