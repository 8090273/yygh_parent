package com.teen.yygh.hosp.receiver;

import com.rabbitmq.client.Channel;
import com.teen.common.rabbitMq.constant.MqConst;
import com.teen.common.rabbitMq.service.RabbitMqService;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.msm.MsmVo;
import com.teen.yygh.vo.order.OrderMqVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MQ监听器
 * @author teen
 * @create 2022/4/7 11:37
 */
@Component
public class HospitalReceiver {
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitMqService rabbitService;

    /**
     * 监听到MQ中有内容则调用方法
     * 更新预约参数，发送短信
     * @param orderMqVo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void receiver(OrderMqVo orderMqVo, Message message, Channel channel) throws IOException {
        //下单成功更新预约数
        Schedule schedule = scheduleService.getScheduleById(orderMqVo.getScheduleId());
        schedule.setReservedNumber(orderMqVo.getReservedNumber());
        schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
        scheduleService.update(schedule);
        //获得短信实体
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            //发送消息给MQ
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

}
