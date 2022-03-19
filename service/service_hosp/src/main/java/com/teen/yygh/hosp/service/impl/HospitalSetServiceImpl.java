package com.teen.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.HttpRequestHelper;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.yygh.hosp.mapper.HospitalSetMapper;
import com.teen.yygh.hosp.service.HospitalSetService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/7 16:52
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
    //获取数据库中的签名
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode",hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        return hospitalSet.getSignKey();
    }

    /**
     * 将验证签名的代码进行封装
     * @param paramMap
     * @return
     */
    @Override
    public void verificationSign(Map<String, Object> paramMap) {
        //验证hoscode是否存在
        if(StringUtils.isEmpty(paramMap.get("hoscode"))){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }


        //将本地签名取出
        String hoscode = (String) paramMap.get("hoscode");
        String hospSetSign = getSignKey(hoscode);

        //使用封装好的工具类来校验
        if (!HttpRequestHelper.isSignEquals(paramMap,hospSetSign)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
}
