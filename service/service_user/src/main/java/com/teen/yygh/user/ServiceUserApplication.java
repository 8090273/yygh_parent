package com.teen.yygh.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author teen
 * @create 2022/3/28 17:31
 */
@SpringBootApplication
@ComponentScan("com.teen")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.teen")
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class,args);
    }
}
