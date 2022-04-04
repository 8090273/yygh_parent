package com.teen.yygh.msm.controller;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.msm.service.MsmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author teen
 * @create 2022/3/29 23:14
 */
@RestController
@RequestMapping("/api/msm")
public class MsmApiController {
    @Autowired
    private MsmService msmService;

    //使用redis
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //发送手机验证码
    @GetMapping("/send/{phone}")
    public Result sendCode(@PathVariable String phone){
        //先从redis中取到验证码，取到则验证，取不到则发送
        //key：手机号  value：验证码
        String code = redisTemplate.opsForValue().get(phone);
        //如果redis中有验证码，直接返回ok
        if(!StringUtils.isEmpty(code)){
            System.out.println("redis中已存在验证码："+code);
            return Result.ok();
        }

        //如果redis中获取不到 则需要发送
        code = msmService.getCode(phone);
        if (code.equals("")){
            return Result.fail().message("验证码发送失败");
        }
        //把验证码放入redis中，key：手机号 value：验证码  过期时间5 单位：分钟  暂时改成5天
        redisTemplate.opsForValue().set(phone,code,5, TimeUnit.DAYS);
        System.out.println("发送成功，验证码是："+code);
        return Result.ok();
    }

}
