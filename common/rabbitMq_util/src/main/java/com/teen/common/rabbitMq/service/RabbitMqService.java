package com.teen.common.rabbitMq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * rabbitMq的服务方法
 * @author teen
 * @create 2022/4/7 10:35
 */
@Service
public class RabbitMqService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     *  发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 消息
     */
    public boolean sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        System.out.println("--------发送MQ完毕___________: "+message.toString());
        return true;
    }

}
