package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Borrower;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowerDetailVO;
import com.atguigu.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author coderxdh
 * @create 2022-04-24 20:38
 */
@Api(tags = "借款人管理接口")
@RestController
@RequestMapping("/admin/core/borrower")
@Slf4j
public class AdminBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人列表")
    @GetMapping("/list/{page}/{limit}")
    public R getBorrowerList(
            @ApiParam(value = "分页查询之页码", required = true)
            @PathVariable Long page,
            @ApiParam(value = "分页查询之每页条数", required = true)
            @PathVariable Long limit,
            @ApiParam(value = "查询关键字", required = false)
            @RequestParam String keyword) {

        Page<Borrower> pageParam = new Page<>(page, limit);
        IPage<Borrower> borrowerIPage = borrowerService.getBorrowerList(pageParam, keyword);
        return R.ok().data("pageModel", borrowerIPage);
    }

    @ApiOperation("获取借款人信息详情")
    @GetMapping("/detail/{borrowerId}")
    public R getBorrowerById(
            @ApiParam(value = "借款人Id", required = true)
            @PathVariable Long borrowerId) {

        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(borrowerId);
        return R.ok().data("borrowerDetailVO", borrowerDetailVO);
    }

    @ApiOperation("借款人额度审批")
    @PostMapping("/approval")
    public R approval(
            @ApiParam(value = "审批对象", required = true)
            @RequestBody BorrowerApprovalVO borrowerApprovalVO) {
        borrowerService.approval(borrowerApprovalVO);
        return R.ok().message("审批完成");
    }
}
