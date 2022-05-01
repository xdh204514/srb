package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.UserInfo;
import com.atguigu.srb.core.pojo.entity.query.UserInfoQuery;
import com.atguigu.srb.core.service.UserInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Api(tags = "会员管理接口")
@RestController
@RequestMapping("/admin/core/userInfo")
@Slf4j
public class AdminUserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public R getUserInfoList(
            @ApiParam(value = "分页查询之每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(value = "分页查询之第几页", required = true)
            @PathVariable Long page,
            @ApiParam(value = "查询条件对象", required = false)
            UserInfoQuery userInfoQuery) {

        final IPage<UserInfo> listPage = userInfoService.listPage(new Page<>(page, limit), userInfoQuery);
        return R.ok().data("pageModel", listPage);
    }

    @ApiOperation("锁定和解锁会员")
    @PutMapping("/lock/{id}/{status}")
    public R lock(
            @ApiParam(value = "会员id", required = true)
            @PathVariable("id") Long id,

            @ApiParam(value = "锁定状态（0：锁定 1：解锁）", required = true)
            @PathVariable("status") Integer status){

        userInfoService.lock(id, status);
        return R.ok().message(status==1?"解锁成功":"锁定成功");
    }

}

