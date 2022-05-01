package com.atguigu.srb.core.pojo.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author coderxdh
 * @create 2022-04-18 20:59
 */
@Data
public class ExcelDictDTO {

    @ExcelProperty("id")
    private Long id;

    @ExcelProperty("上级id")
    private Long parentId;

    @ExcelProperty("名称")
    private String name;

    @ExcelProperty("值")
    private Integer value;

    @ExcelProperty("编码")
    private String dictCode;
}