package com.teen.yygh.hosp.client;

import com.teen.yygh.vo.hosp.ScheduleOrderVo;
import com.teen.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author teen
 * @create 2022/4/6 19:01
 */
@FeignClient("service-hosp")
@Repository
public interface HospitalFeignClient {
    //调用医院模块接口

    // 根据科室id获取 科室订单 实体
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);

    //获取医院的签名信息
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);
}
