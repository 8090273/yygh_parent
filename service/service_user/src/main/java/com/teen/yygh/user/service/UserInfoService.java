package com.teen.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teen.yygh.model.user.UserInfo;
import com.teen.yygh.vo.user.LoginVo;
import com.teen.yygh.vo.user.UserAuthVo;
import com.teen.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/28 17:49
 */

public interface UserInfoService extends IService<UserInfo>{
    Map<String, Object> loginByPhone(LoginVo loginVo);

    void userAuth(Long userId, UserAuthVo userAuthVo);

    IPage<UserInfo> selectPage(Page<UserInfo> userInfoPage, UserInfoQueryVo userInfoQueryVo);

    void lock(String id, Integer status);

    Map<String, Object> show(Long id);
}
