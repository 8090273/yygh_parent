package com.teen.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author teen
 * @create 2022/3/29 22:30
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
@ComponentScan("com.teen")
public class ServiceMsmApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsmApplication.class,args);
    }
}
