package com.atguigu.srb.core.pojo.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author coderxdh
 * @create 2022-04-29 20:00
 */
@Data
@ApiModel(description = "投标信息")
public class InvestVO {

    @ApiModelProperty(value = "标的Id")
    private Long lendId;

    @ApiModelProperty(value = "本次投资金额")
    private String investAmount;

    @ApiModelProperty(value = "本次投资人Id")
    private Long investUserId;

    @ApiModelProperty(value = "本次投资人姓名")
    private String investName;
}
