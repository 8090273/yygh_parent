package com.teen.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.model.user.UserInfo;
import com.teen.yygh.user.service.UserInfoService;
import com.teen.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author teen
 * @create 2022/4/3 9:57
 */
@Api(tags = "用于平台的用户管理接口")
@RestController
@RequestMapping("admin/user")
public class UserController {
    @Autowired
    private UserInfoService userInfoService;

    //查询用户列表（条件查询带分页）
    @ApiOperation(value = "分页查询用户列表")
    @GetMapping("{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> userInfoPage = new Page<>(page, limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(userInfoPage,userInfoQueryVo);
        return Result.ok(pageModel);
    }

    //用户锁定
    @ApiOperation(value = "改变用户状态")
    @GetMapping("lock/{id}/{status}")
    public Result lock(@PathVariable String id,@PathVariable Integer status){
        userInfoService.lock(id,status);
        return Result.ok();
    }
    //用户详情
    @ApiOperation(value = "根据id查询详情")
    @GetMapping("show/{id}")
    public Result show(@PathVariable Long id){
        Map<String,Object> map = userInfoService.show(id);
        return Result.ok(map);
    }

    //审批用户认证
    @ApiOperation(value = "审批用户的实名认证信息")
    @GetMapping("approval/{id}/{authStatus}")
    public Result approval(@PathVariable Long id,
                           @ApiParam(name = "authStatus",value = "认证状态（0：不验证；2：通过；-1：不通过）") @PathVariable Integer authStatus){
        //只有当传来-1或2时才更新
        if (authStatus==-1||authStatus==2){
            UserInfo userInfo = userInfoService.getById(id);
            userInfo.setAuthStatus(authStatus);
            userInfoService.updateById(userInfo);
        }
        return Result.ok();
    }
}
