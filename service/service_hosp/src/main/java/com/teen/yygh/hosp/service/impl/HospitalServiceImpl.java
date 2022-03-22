package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.dict.client.DictFeignClient;
import com.teen.yygh.hosp.repository.HospitalRepository;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    //要使用openFeign，所以注入依赖
    @Autowired
    private DictFeignClient dictFeignClient;

    /**
     * 保存或修改医院信息到MongoDB中
     * @param paramMap
     */
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

    /**
     * 分页查询管理所有医院方
     * @param page
     * @param limit
     * @param hospitalQueryVo
     * @return
     */
    @Override
    public Page<Hospital> selectHospPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //凭自己感觉写写试试？ 写出来辣~

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //设置排序规则
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //核心 分页对象
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example example = Example.of(hospital,matcher);

        //使用Repository查询出mongodb中的数据
        Page<Hospital> pages = hospitalRepository.findAll(example,pageable);
        //从pages中取出对象  取出的是mongodb中所有医院的详细信息，但其中不包括医院等级，医院等级在dict中，需要远程调用dict微服务中的接口获取医院等级
//        List<Hospital> hospitalList = pages.getContent(); 这是原写法
        //Hospital类中有一个param属性，是一个HashMap集合，里边可添加其他参数
        //可改成java8新特性中的stream()流写法+lambda表达式写法
        pages.getContent().stream().forEach(item -> {
            this.setHospitalHosType(item);
        });


        return pages;
    }


    /**
     * 修改医院的上线状态
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(String id, Integer status) {
        //根据id查询出对象
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);
    }

    /**
     * 通过id获取医院的具体信息
     * @param id
     * @return
     */
    @Override
    public HashMap<String, Object> getHospitalById(String id) {
        HashMap<String, Object> map = new HashMap<>();
        Hospital hospital = hospitalRepository.findById(id).get();
        hospital = this.setHospitalHosType(hospital);
        map.put("hospital",hospital);
        map.put("bookingRule",hospital.getBookingRule());

        //因为不需要重复返回，所以把实体中的属性清空
        hospital.setBookingRule(null);
        return map;
    }

    /**
     * 通过远程调用数据字典获取医院详情
     * @param hospital
     * @return
     */
    private Hospital setHospitalHosType(Hospital hospital) {
        //在这里使用feign远程调用微服务完成业务
        //根据dictCode和value（Hostype）来获取医院等级
        String hospitalLevel = dictFeignClient.getDickName("Hostype", hospital.getHostype());

        //练习
        //查询省
        String province = dictFeignClient.getDictName(hospital.getProvinceCode());
        //查询市
        String city = dictFeignClient.getDictName(hospital.getCityCode());
        //地区
        String district = dictFeignClient.getDictName(hospital.getDistrictCode());
        //练习end
        //并放入param属性中
        hospital.getParam().put("Address",province+city+district);
        hospital.getParam().put("hospitalLevel",hospitalLevel);
        return hospital;
    }
}
