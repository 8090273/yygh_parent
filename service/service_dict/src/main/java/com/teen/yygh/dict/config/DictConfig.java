package com.teen.yygh.dict.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author teen
 * @create 2022/3/7 17:08
 */

@Configuration
@MapperScan("com.teen.yygh.dict.mapper")
public class DictConfig {
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
