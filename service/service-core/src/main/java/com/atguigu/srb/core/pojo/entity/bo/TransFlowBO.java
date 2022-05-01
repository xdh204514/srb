package com.atguigu.srb.core.pojo.entity.bo;

import com.atguigu.srb.core.enums.TransTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author coderxdh
 * @create 2022-04-28 10:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description="交易流水对象")
public class TransFlowBO {

    @ApiModelProperty(value = "交易编号")
    private String agentBillNo;
    @ApiModelProperty(value = "交易用户的 bind_code")
    private String bindCode;
    @ApiModelProperty(value = "交易金额")
    private BigDecimal amount;
    @ApiModelProperty(value = "交易类型")
    private TransTypeEnum transTypeEnum;
    @ApiModelProperty(value = "交易说明")
    private String memo;
}