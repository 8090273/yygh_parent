package com.teen.yygh.common.util;

import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.JwtHelper;
import com.teen.yygh.common.result.ResultCodeEnum;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取当前用户信息工具
 * @author teen
 * @create 2022/4/1 14:47
 */
public class AuthContextHolder {
    //获取当前用户id
    public static Long getUserId(HttpServletRequest request){
        //从header中获取token
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)){
            System.out.println("token为空！");
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        Long userId = JwtHelper.getUserId(token);

        return userId;
    }

    //获取当前用户名称
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        String userName = JwtHelper.getUserName(token);
        return userName;
    }
}
