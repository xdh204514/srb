package com.atguigu.srb.core.controller.api;


import com.atguigu.srb.common.result.R;
import com.atguigu.srb.core.pojo.entity.Dict;
import com.atguigu.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 * @author CoderXdh
 * @since 2022-04-17
 */
@Api(tags = "数字字典接口")
@RestController
@RequestMapping("/api/core/dict")
@Slf4j
public class ApiDictController {

    @Resource
    private DictService dictService;

    @ApiOperation("根据dictCode获取下级节点列表")
    @GetMapping("/getDictByDictCode/{dictCode}")
    public R getDictByDictCode(
            @ApiParam(value = "dictCode", required = true)
            @PathVariable String dictCode) {

        List<Dict> dictList = dictService.getDictByDictCode(dictCode);
        return R.ok().data("records", dictList);
    }
}

