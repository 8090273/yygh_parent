package com.teen.yygh.vo.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description="登录对象")
public class LoginVo {
    //微信openId
    @ApiModelProperty(value = "微信openid")
    private String openid;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "验证码")
    private String code;

    @ApiModelProperty(value = "IP")
    private String ip;
}
