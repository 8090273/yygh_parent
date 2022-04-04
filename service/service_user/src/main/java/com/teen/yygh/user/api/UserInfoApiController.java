package com.teen.yygh.user.api;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.common.util.AuthContextHolder;
import com.teen.yygh.model.user.UserInfo;
import com.teen.yygh.user.service.UserInfoService;
import com.teen.yygh.vo.user.LoginVo;
import com.teen.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
        System.out.println("登录成功");
        return Result.ok(info);
    }

    //用户认证
    @ApiOperation( value = "用户实名认证接口")
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){
        //用户认证服务，传递userId和认证Vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();

    }

    @ApiOperation(value = "根据用户id获取用户信息")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }
}
