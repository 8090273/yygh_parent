package com.teen.common.rabbitMq.constant;

/**
 * @author teen
 * @create 2022/4/7 10:46
 */
public class MqConst {
    /**
     * 预约下单
     */
    //交换机
    public static final String EXCHANGE_DIRECT_ORDER = "exchange.direct.order";
    //路由
    public static final String ROUTING_ORDER = "order";
    //队列
    public static final String QUEUE_ORDER  = "queue.order";
    /**
     * 短信
     */
    public static final String EXCHANGE_DIRECT_MSM = "exchange.direct.msm";
    public static final String ROUTING_MSM_ITEM = "msm.item";
    //队列
    public static final String QUEUE_MSM_ITEM  = "queue.msm.item";

}
