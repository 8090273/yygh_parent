package com.teen.yygh.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author teen
 * @create 2022/4/6 16:04
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.teen"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.teen"})
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class,args);
    }
}
