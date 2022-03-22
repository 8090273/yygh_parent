package com.teen.yygh.dict;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author teen
 * @create 2022/3/15 16:26
 */
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = {"com.teen"})
public class DictApplication {
    public static void main(String[] args) {
        SpringApplication.run(DictApplication.class,args);
    }
}
