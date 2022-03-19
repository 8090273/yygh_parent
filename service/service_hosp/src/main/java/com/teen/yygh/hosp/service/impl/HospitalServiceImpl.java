package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.hosp.repository.HospitalRepository;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/17 10:12
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    //因为service要调用mongoDB，所以注入依赖
    @Autowired
    private HospitalRepository hospitalRepository;

    @Override
    public void save(Map<String, Object> paramMap) {
        //把形参转换为实体类
        //把map转为JSON字符串，再将JSON字符串转为实体类
        String mapJSON = JSONObject.toJSONString(paramMap);
        Hospital hospital = JSONObject.parseObject(mapJSON, Hospital.class);
        //根据医院code判断数据库中是否有值
        String hoscode = hospital.getHoscode();
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hoscode);


        if(hospitalExist == null){
            //没有则添加
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);

        }else {
            //有则修改
            hospital.setId(hospitalExist.getId());
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            //修改完成
            hospitalRepository.save(hospital);
        }


    }

    @Override
    public Hospital getByHoscode(String hoscode) {

        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;

    }
}
