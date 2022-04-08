package com.teen.yygh.user.client;

import com.teen.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author teen
 * @create 2022/4/6 17:53
 */
@FeignClient("service-user")
@Repository
public interface PatientFeignClient {
    //调用user微服务的接口

    @GetMapping("api/user/patient/inner/get/{id}")
    public Patient getPatientOrder(@PathVariable("id") Long id);
}
