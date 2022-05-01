package com.atguigu.srb.core.controller.admin;

import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoApprovalVO;
import com.atguigu.srb.core.pojo.entity.vo.BorrowInfoVO;
import com.atguigu.srb.core.service.BorrowInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author coderxdh
 * @create 2022-04-26 10:59
 */
@Api(tags = "借款信息管理接口")
@RestController
@RequestMapping("/admin/core/borrowInfo")
@Slf4j
public class AdminBorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("借款信息列表")
    @GetMapping("/list/{page}/{limit}")
    public R getBorrowInfoList(@ApiParam(value = "分页查询之页码", required = true)
                      @PathVariable Long page,
                  @ApiParam(value = "分页查询之每页条数", required = true)
                      @PathVariable Long limit,
                  @ApiParam(value = "查询关键字", required = false)
                      @RequestParam String keyword) {
        Page<BorrowInfoVO> pageParam = new Page<>(page, limit);
        IPage<BorrowInfoVO> borrowerInfoVOIPage = borrowInfoService.getBorrowInfoVOList(pageParam, keyword);
        return R.ok().data("pageModel", borrowerInfoVOIPage);
    }

    @ApiOperation("获取借款信息详情")
    @GetMapping("/getBorrowInfoDetail/{id}")
    public R getBorrowInfoDetail(
            @ApiParam(value = "借款信息Id", required = true)
            @PathVariable Long id) {
        Map<String, Object> borrowInfoDetail = borrowInfoService.getBorrowInfoDetail(id);
        return R.ok().data("borrowInfoDetail", borrowInfoDetail);
    }

    @ApiOperation("审批借款信息")
    @PostMapping("/approval")
    public R approval(@RequestBody BorrowInfoApprovalVO borrowInfoApprovalVO) {

        borrowInfoService.approval(borrowInfoApprovalVO);
        return R.ok().message("审批完成");
    }
}
