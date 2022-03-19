package com.teen.yygh.hosp.service;

import com.teen.yygh.model.hosp.Hospital;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/17 10:11
 */
public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getByHoscode(String hoscode);
}
