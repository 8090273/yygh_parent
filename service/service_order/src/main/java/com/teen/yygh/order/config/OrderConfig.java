package com.teen.yygh.order.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author teen
 * @create 2022/4/6 16:58
 */
@Configuration
@MapperScan("com.teen.yygh.order.mapper")
public class OrderConfig {
}
