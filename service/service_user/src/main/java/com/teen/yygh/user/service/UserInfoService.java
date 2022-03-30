package com.teen.yygh.user.service;

import com.teen.yygh.vo.user.LoginVo;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/28 17:49
 */

public interface UserInfoService {
    Map<String, Object> loginByPhone(LoginVo loginVo);
}
