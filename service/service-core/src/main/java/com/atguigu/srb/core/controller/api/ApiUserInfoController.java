package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.base.utils.JwtUtil;
import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.common.utils.RegexValidateUtil;
import com.atguigu.srb.core.pojo.entity.vo.LoginVO;
import com.atguigu.srb.core.pojo.entity.vo.RegisterVO;
import com.atguigu.srb.core.pojo.entity.vo.UserIndexVO;
import com.atguigu.srb.core.pojo.entity.vo.UserInfoVO;
import com.atguigu.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Api(tags = "用户接口")
@RestController
@RequestMapping("/api/core/userInfo")
@Slf4j
public class ApiUserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public R register(
            @ApiParam(value = "用户信息", required = true)
            @RequestBody RegisterVO registerVO) {

        // 1. 获取参数
        String mobile = registerVO.getMobile();
        String password = registerVO.getPassword();
        String code = registerVO.getCode();

        // 1. 参数校验
        // MOBILE_NULL_ERROR(-202, "手机号码不能为空")
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        // MOBILE_ERROR(-203, "手机号码不正确")
        Assert.isTrue(RegexValidateUtil.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        // CODE_NULL_ERROR(205, "验证码不能为空")
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);
        String redisCode = (String) redisTemplate.opsForValue().get("srb:sms:code:" + mobile);
        // CODE_ERROR(206, "验证码错误")
        Assert.equals(code, redisCode, ResponseEnum.CODE_ERROR);
        // PASSWORD_NULL_ERROR(204, "密码不能为空")
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        // 2. 注册用户
        userInfoService.register(registerVO);

        // 3. 返回
        return R.ok().message("注册成功!");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public R login(@ApiParam(value = "用户对象", required = true)
                   @RequestBody LoginVO loginVO,
                   HttpServletRequest request) {

        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        // 判断手机号是否为空
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        // 判断密码是否为空
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        // 获取登录用户的 ip 地址，在 user_login_record 表中需要记录用户的登录日志
        String ip = request.getRemoteAddr();

        UserInfoVO userInfoVO = userInfoService.login(loginVO, ip);

        return R.ok().message("登陆成功!").data("userInfo", userInfoVO);
    }

    @ApiOperation("令牌校验")
    @GetMapping("/checkToken")
    public R checkToken(HttpServletRequest request) {
        String token = request.getHeader("token");
        boolean result = JwtUtil.checkToken(token);

        if(result){
            return R.ok();
        }else{
            // LOGIN_AUTH_ERROR(-211, "未登录"),
            return R.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }

    @ApiOperation("校验手机号是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public R checkMobile(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile) {
        boolean result = userInfoService.checkMobile(mobile);
        return R.ok().data("isExist", result);
    }

    @ApiOperation("获取个人空间用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public R getIndexUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        UserIndexVO userIndexVO = userInfoService.getIndexUserInfo(userId);
        return R.ok().data("userIndexVO", userIndexVO);
    }
}

