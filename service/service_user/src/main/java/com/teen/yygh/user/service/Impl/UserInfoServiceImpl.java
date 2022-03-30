package com.teen.yygh.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.JwtHelper;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.model.user.UserInfo;
import com.teen.yygh.user.mapper.UserInfoMapper;
import com.teen.yygh.user.service.UserInfoService;
import com.teen.yygh.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/28 17:49
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper,UserInfo> implements UserInfoService {
    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> loginByPhone(LoginVo loginVo) {
        //从对象中取出手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        //验证是否为空
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 验证验证码
        //从redis中取出验证码
        String redisCode = redisTemplate.opsForValue().get(phone).toString();
        if (StringUtils.isEmpty(redisCode)){
            System.out.println("redis中验证码为空");
        }

        if (!code.equals(redisCode)){
            System.out.println("验证码不一致");
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }


        //判断是否是第一次登录，如果是第一次登录，直接注册
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("phone",phone);
        UserInfo userInfo = this.getOne(wrapper);
        if (userInfo == null){
            //注册
            userInfo = new UserInfo();
            userInfo.setPhone(phone);
            userInfo.setName("");
            userInfo.setStatus(1); //1表示未封号
            this.save(userInfo);
        }

        //检测是否被封号
        if (userInfo.getStatus() == 0){
            //该用户已被禁用
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }

        //返回页面的昵称信息
        HashMap<String,Object> map = new HashMap<>();
        String name = userInfo.getName();
        if (StringUtils.isEmpty(name)){
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)){
            name = userInfo.getPhone();
        }
        map.put("name",name);

        // 生成token
        String token = JwtHelper.createToken(userInfo.getId(),name);
        map.put("token",token);

        return map;

    }
}
