package com.atguigu.srb.core.pojo.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author coderxdh
 * @create 2022-04-21 12:03
 */
@Data
@ApiModel(description="用户信息对象")
public class UserInfoVO {

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @ApiModelProperty(value = "头像")
    private String headImg;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "1：出借人 2：借款人")
    private Integer userType;

    @ApiModelProperty(value = "JWT访问令牌")
    private String token;
}
