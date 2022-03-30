package com.teen.yygh.user.controller;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.user.service.UserInfoService;
import com.teen.yygh.vo.user.LoginVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/28 17:48
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {

    @Autowired
    UserInfoService userInfoService;

    //根据手机号登录
    @ApiOperation(value = "根据手机号来登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String ,Object> info = userInfoService.loginByPhone(loginVo);
        return Result.ok(info);
    }
}
