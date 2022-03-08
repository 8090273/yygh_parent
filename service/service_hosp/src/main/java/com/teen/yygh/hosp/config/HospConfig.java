package com.teen.yygh.hosp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author teen
 * @create 2022/3/7 17:08
 */

@Configuration
@MapperScan("com.teen.yygh.hosp.mapper")
public class HospConfig {
}
