package com.teen.yygh.order.service;

import java.util.Map;

/**
 * @author teen
 * @create 2022/4/8 23:09
 */
public interface WeixinService {
    Map createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId, String name);

    Boolean refund(Long orderId);
}
