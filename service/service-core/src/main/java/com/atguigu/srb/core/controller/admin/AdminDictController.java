package com.atguigu.srb.core.controller.admin;


import com.alibaba.excel.EasyExcel;
import com.atguigu.srb.common.exception.BusinessException;
import com.atguigu.srb.common.result.R;
import com.atguigu.srb.common.result.ResponseEnum;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.pojo.entity.ExcelDictDTO;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Api(tags = "数据字典管理接口")
@RestController
@RequestMapping("/admin/core/dict")
@Slf4j
public class AdminDictController {

    @Resource
    private DictService dictService;

    @ApiOperation("Excel批量导入数据字典")
    @PostMapping("/import")
    public R importExcelData(
            @ApiParam(value = "Excel数据字典文件", required = true)
            @RequestParam("file") MultipartFile file) {

        try {
            val inputStream = file.getInputStream();
            dictService.importExcelData(inputStream);

            return R.ok().message("Excel数据字典上传成功");
        } catch (Exception e) {
            // 抛出自定义异常
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("Excel批量导出数据字典")
    @GetMapping("/export")
    public void exportExcelData(HttpServletResponse response) {

        try {
            // 这里注意 有同学反应使用 swagger 会导致各种问题，请直接用浏览器或者用 postman
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里 URLEncoder.encode 可以防止中文乱码 当然和 easyexcel 没有关系
            String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("数据字典").doWrite(dictService.getDictData());

        } catch (IOException e) {
            // EXPORT_DATA_ERROR(104, "数据导出失败"),
            throw  new BusinessException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }

    @ApiOperation("根据上级id获取子节点数据列表")
    @GetMapping("/getDictDataByParentId/{parentId}")
    public R getDictDataByParentId(
            @ApiParam(value = "上级节点id", required = true)
            @PathVariable Long parentId) {

        List<Dict> dictList = dictService.getDictDataByParentId(parentId);
        if (dictList != null) {
            return R.ok().data("records", dictList);
        } else {
            return R.error().message("获取失败！");
        }
    }
}

