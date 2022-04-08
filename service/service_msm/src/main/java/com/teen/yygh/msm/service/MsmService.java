package com.teen.yygh.msm.service;

import com.teen.yygh.vo.msm.MsmVo;

/**
 * @author teen
 * @create 2022/3/29 23:16
 */
public interface MsmService {
    //登录获取验证码
    String getCode(String phone);
    // MQ发送短信
    String send(MsmVo msmVo);

}
