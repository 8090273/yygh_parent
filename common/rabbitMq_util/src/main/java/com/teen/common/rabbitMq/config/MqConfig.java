package com.teen.common.rabbitMq.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author teen
 * @create 2022/4/7 10:38
 */
@Configuration
public class MqConfig {

    /**
     * 消息转换器
     * 默认是字符串转换器
     * @return
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
