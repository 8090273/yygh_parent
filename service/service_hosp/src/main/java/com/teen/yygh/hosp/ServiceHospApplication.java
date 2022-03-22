package com.teen.yygh.hosp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author teen
 * @create 2022/3/7 16:26
 */
@EnableDiscoveryClient  //入驻注册中心
@SpringBootApplication
@ComponentScan(basePackages = "com.teen")
@EnableFeignClients(basePackages = "com.teen") //开启服务调用
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class,args);
    }
}
