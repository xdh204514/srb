package com.atguigu.srb.core.controller.admin;


import com.atguigu.srb.common.exception.Assert;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.IntegralGrade;
import com.atguigu.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Api(tags = "后台积分等级列表管理接口")  // Swagger2 的注解
@RestController
@RequestMapping("/admin/core/integralGrade")
@Slf4j
public class AdminIntegralGradeController {

    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation("获取积分等级列表")
    @GetMapping("/list")
    public R getAllList() {
        List<IntegralGrade> list = integralGradeService.list();
        return R.ok().data("records", list);
    }

    @ApiOperation(value = "删除积分等级", notes = "根据ID进行逻辑删除")
    @DeleteMapping("/remove/{id}")
    public R removeById(
            @ApiParam(value = "数据id", required = true, example = "1")
            @PathVariable Long id) {
        boolean b = integralGradeService.removeById(id);
        if (b) {
            return R.ok().message("删除成功");
        } else {
            return R.error().message("删除失败");
        }
    }

    @ApiOperation("保存积分等级")
    @PostMapping("/save")
    public R save(
            @ApiParam(value = "IntegralGrade 对象")
            @RequestBody IntegralGrade integralGrade) {

//        if (integralGrade.getBorrowAmount() == null) {
//            throw new BusinessException(ResponseEnum.BORROW_AMOUNT_NULL_ERROR);
//        }
        // 使用断言去判断是否为空
        Assert.notNull(integralGrade.getBorrowAmount(), ResponseEnum.BORROW_AMOUNT_NULL_ERROR);

        boolean b = integralGradeService.save(integralGrade);
        if (b) {
            return R.ok().message("保存成功");
        } else {
            return R.error().message("保存失败");
        }
    }

    @ApiOperation("更新积分等级")
    @PutMapping("/update")
    public R updateById(
            @ApiParam(value = "IntegralGrade 对象")
            @RequestBody IntegralGrade integralGrade) {

        boolean b = integralGradeService.updateById(integralGrade);
        if (b) {
            return R.ok().message("更新成功");
        } else {
            return R.error().message("更新失败");
        }
    }

    @ApiOperation(value = "获取积分等级", notes = "根据 ID 获取")
    @GetMapping("/get/{id}")
    public R getById(
            @ApiParam(value = "id值")
            @PathVariable Long id) {

        IntegralGrade grade = integralGradeService.getById(id);
        if (grade != null) {
            return R.ok().data("record", grade);
        } else {
            return R.error();
        }
    }
}

