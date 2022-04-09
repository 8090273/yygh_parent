package com.teen.yygh.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.yygh.enums.RefundStatusEnum;
import com.teen.yygh.model.order.PaymentInfo;
import com.teen.yygh.model.order.RefundInfo;
import com.teen.yygh.order.mapper.RefundInfoMapper;
import com.teen.yygh.order.service.RefundInfoService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author teen
 * @create 2022/4/9 15:46
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {
    /**
     * 保存退款记录
     * @param paymentInfo
     * @return
     */
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",paymentInfo.getOrderId());
        queryWrapper.eq("payment_type",paymentInfo.getPaymentType());
        RefundInfo refundInfo = this.getOne(queryWrapper);
        //如果存在退款记录
        if (null != refundInfo){
            System.out.println("数据库中已经有退款数据了");
            return refundInfo;
        }
        //不存在退款记录则保存
        refundInfo = new RefundInfo();
        //创建时间：当前系统时间
        refundInfo.setCreateTime(new Date());
        //订单编号
        refundInfo.setOrderId(paymentInfo.getOrderId());
        //支付类型：微信
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        // 对外业务编号
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        // 退款状态：退款中
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        // 交易内容
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        // 退款金额
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        //保存
        this.save(refundInfo);

        return refundInfo;
    }
}
