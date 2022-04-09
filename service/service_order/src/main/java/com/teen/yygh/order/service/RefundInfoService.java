package com.teen.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teen.yygh.model.order.PaymentInfo;
import com.teen.yygh.model.order.RefundInfo;

/**
 * 保存退款记录
 * @author teen
 * @create 2022/4/9 15:45
 */
public interface RefundInfoService extends IService<RefundInfo> {
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
