package com.teen.yygh.hosp.service;

import com.teen.yygh.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/7 16:51
 */
public interface HospitalSetService extends IService<HospitalSet> {
    String getSignKey(String hoscode);

    void verificationSign(Map<String, Object> paramMap);
}
