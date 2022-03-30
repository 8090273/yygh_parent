package com.teen.yygh.user.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author teen
 * @create 2022/3/28 18:00
 */
@Configuration
@MapperScan("com.teen.yygh.user.mapper")
public class UserConfig {
}
