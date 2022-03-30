package com.teen.yygh.hosp.service;

import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/17 10:11
 */
public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    HashMap<String,Object> getHospitalById(String id);

    String getHospitalNameByHoscode(String hoscode);

    List<Hospital> findByHosname(String hosname);

    Map<String, Object> findBookingRuleDetailByHoscode(String hoscode);
}
