package com.teen.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teen.yygh.model.order.OrderInfo;
import com.teen.yygh.model.order.PaymentInfo;

import java.util.Map;

/**
 * @author teen
 * @create 2022/4/8 23:18
 */
public interface PaymentService extends IService<PaymentInfo> {
    void savePaymentInfo(OrderInfo orderInfo, Integer status);

    void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> resultMap);

    PaymentInfo getPaymentInfo(Long orderId,Integer paymentType);
}
