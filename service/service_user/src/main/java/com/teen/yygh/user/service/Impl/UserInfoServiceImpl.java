package com.teen.yygh.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.JwtHelper;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.enums.AuthStatusEnum;
import com.teen.yygh.model.user.Patient;
import com.teen.yygh.model.user.UserInfo;
import com.teen.yygh.user.mapper.UserInfoMapper;
import com.teen.yygh.user.service.PatientService;
import com.teen.yygh.user.service.UserInfoService;
import com.teen.yygh.vo.user.LoginVo;
import com.teen.yygh.vo.user.UserAuthVo;
import com.teen.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private PatientService patientService;

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
            System.out.println(userInfo.getPhone()+"注册成功");

        }

        //检测是否被封号
        if (userInfo.getStatus() == 0){
            //该用户已被禁用
            System.out.println("用户"+userInfo.getPhone()+"已被封号");
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

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = this.getById(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        //这里错误设置为了status导致无法得到认证数据
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());

        //bug在这里，不应该用save方法，应用用updateById方法
        this.updateById(userInfo);
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> userInfoPage, UserInfoQueryVo userInfoQueryVo) {
        //通过queryVo获取条件
        String name = userInfoQueryVo.getKeyword();//获取用户名称
        Integer status = userInfoQueryVo.getStatus(); //获取用户锁定状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //获取用户认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //判断条件是否是空（很容易为空）
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name))
            wrapper.like("name",name);
        if (!StringUtils.isEmpty(status))
            wrapper.eq("status",status);
        if (!StringUtils.isEmpty(authStatus))
            wrapper.eq("auth_status",authStatus);
        if (!StringUtils.isEmpty(createTimeBegin))
            //ge：大于等于
            wrapper.ge("create_time",createTimeBegin);
        if (!StringUtils.isEmpty(createTimeEnd))
            //le:小于等于
            wrapper.le("create_time",createTimeEnd);
        //通过baseMapper查询出的page对象中的数据并不完整，应根据数据字典翻译编码
        Page<UserInfo> page = baseMapper.selectPage(userInfoPage, wrapper);
        page.getRecords().forEach(this::packageUserInfo);

        return page;
    }

    /**
     * 根据id修改用户锁定状态
     * @param id
     * @param status
     */
    @Override
    public void lock(String id, Integer status) {
        //若status值为1或0才进行修改
        if (status.intValue()==0||status.intValue()==1){
            UserInfo userInfo = this.getById(id);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long id) {
        Map<String, Object> map = new HashMap<>();

        //查询用户信息
        UserInfo userInfo = this.getById(id);
        this.packageUserInfo(userInfo);
        map.put("userInfo",userInfo);
        //查询就诊人信息
        List<Patient> patientList = patientService.findAllByUserId(id);
        map.put("patientList",patientList);

        return map;
    }

    /**
     * 根据用户id退出登录
     * @param userId
     */
    @Override
    public void logoutByUserId(Long userId) {
        System.out.println("----------------正在退出登录--------------");
        if (userId ==null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        UserInfo userInfo = this.getById(userId);
        if (userInfo == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        String phone = userInfo.getPhone();
        //手机号是否为空
        if (StringUtils.isEmpty(phone)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //从redis中取出验证码
        String redisCode = redisTemplate.opsForValue().get(phone).toString();
        System.out.println("redis中的验证码是："+ redisCode);
        //如果redis中的验证码不为空
        if (!StringUtils.isEmpty(redisCode)){
            redisTemplate.delete(phone);
            System.out.println("清空了redis的验证码！");
        }
    }


    private UserInfo packageUserInfo(UserInfo userInfo) {
        //翻译认证状态编码
        Integer authStatus = userInfo.getAuthStatus();
        userInfo.getParam().put("authStatusStrins",AuthStatusEnum.getStatusNameByStatus(authStatus));
        //处理用户状态
        String statusString = userInfo.getStatus() ==0 ? "锁定":"正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }


}
