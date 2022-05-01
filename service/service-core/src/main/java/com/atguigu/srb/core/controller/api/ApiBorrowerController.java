package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.base.utils.JwtUtil;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerVO;
import com.atguigu.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Api(tags = "借款人接口")
@RestController
@RequestMapping("/api/core/borrower")
@Slf4j
public class ApiBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("保存借款人认证信息")
    @PostMapping("/auth/save")
    public R save(
            @ApiParam(value = "借款人对象", required = true)
            @RequestBody BorrowerVO borrowerVO,
            HttpServletRequest request) {

        final String token = request.getHeader("token");
        final Long userId = JwtUtil.getUserId(token);
        borrowerService.saveByUserId(borrowerVO, userId);
        return R.ok().message("保存成功");
    }

    @ApiOperation("获取借款人认证状态")
    @GetMapping("/auth/getBorrowerStatus")
    public R getBorrowerStatus(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtil.getUserId(token);
        Integer status = borrowerService.getStatusByUserId(userId);
        return R.ok().data("borrowerStatus", status);
    }
}

