package com.teen.yygh.msm.receiver;

import com.rabbitmq.client.Channel;
import com.teen.common.rabbitMq.constant.MqConst;
import com.teen.yygh.msm.service.MsmService;
import com.teen.yygh.vo.msm.MsmVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * MQ监听器
 * @author teen
 * @create 2022/4/7 11:00
 */
@Component
@Lazy(false)
public class MsmReceive {
    @Autowired
    private MsmService msmService;

    /**
     * 如果监听到mq中有内容，则进行方法调用
     * @param msmVo
     * @param message
     * @param channel
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(MsmVo msmVo, Message message, Channel channel) {
        System.out.println("---------监听到了MQ中的内容！-------------");
        msmService.send(msmVo);
        System.out.println("-------------MQ触发后发送短信成功--------------");
    }

}
