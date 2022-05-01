package com.atguigu.srb.core.controller.admin;

/**
 * @author coderxdh
 * @create 2022-04-27 20:14
 */

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Lend;
import com.atguigu.srb.core.service.LendService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@Api(tags = "投资管理接口")
@RestController
@RequestMapping("/admin/core/lend")
@Slf4j
public class AdminLendController {

    @Resource
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list/{page}/{limit}")
    public R getLendList(@ApiParam(value = "分页查询之页码", required = true)
                         @PathVariable Long page,
                         @ApiParam(value = "分页查询之每页条数", required = true)
                         @PathVariable Long limit,
                         @ApiParam(value = "查询关键字", required = false)
                         @RequestParam String keyword) {
        Page<Lend> pageParam = new Page<>(page, limit);
        IPage<Lend> lendPageModel = lendService.getLendList(pageParam, keyword);
        return R.ok().data("pageModel", lendPageModel);
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/getLendDetail/{id}")
    public R getLendDetail(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long id) {
        Map<String, Object> result = lendService.getLendDetail(id);
        return R.ok().data("lendDetail", result);
    }

    @ApiOperation("放款")
    @GetMapping("/makeLoan/{id}")
    public R makeLoan(
            @ApiParam(value = "标的id", required = true)
            @PathVariable("id") Long id) {
        lendService.makeLoan(id);
        return R.ok().message("放款成功");
    }
}
