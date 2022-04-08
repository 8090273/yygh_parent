package com.teen.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teen.yygh.model.order.OrderInfo;
import com.teen.yygh.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * @author teen
 * @create 2022/4/6 16:45
 */
public interface OrderService extends IService<OrderInfo> {
    Long saveOrder(String scheduleId, Long patientId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    OrderInfo getOrder(String orderId);

    Map<String,Object> show(Long id);
}
