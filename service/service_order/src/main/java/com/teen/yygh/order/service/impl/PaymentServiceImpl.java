package com.teen.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.corba.se.spi.orbutil.threadpool.WorkQueue;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.HttpRequestHelper;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.enums.OrderStatusEnum;
import com.teen.yygh.enums.PaymentStatusEnum;
import com.teen.yygh.hosp.client.HospitalFeignClient;
import com.teen.yygh.model.order.OrderInfo;
import com.teen.yygh.model.order.PaymentInfo;
import com.teen.yygh.order.mapper.PaymentMapper;
import com.teen.yygh.order.service.OrderService;
import com.teen.yygh.order.service.PaymentService;
import com.teen.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author teen
 * @create 2022/4/8 23:19
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {
    @Autowired
    private OrderService orderService;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;


    /**
     * 向支付记录表中添加信息
     * @param orderInfo
     * @param paymentType
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        //查询是否存在相同的订单
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderInfo.getId());
        wrapper.eq("payment_type",paymentType);
        int count = this.count(wrapper);
        if (count > 0){
            System.out.println("已存在相同的订单！");
            return;
        }
        //添加记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        //订单交易号
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        this.save(paymentInfo);

    }

    /**
     * 支付成功后更新订单状态
     * @param out_trade_no
     * @param paymentType
     * @param resultMap
     */
    @Override
    public void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> resultMap) {
        // 根据订单id得到支付记录
        PaymentInfo paymentInfo = this.getPaymentInfo(out_trade_no,paymentType);
        if (null == paymentInfo){
            System.out.println("支付记录查询失败！！");
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //如果当前支付记录的支付状态不是 正在支付
        if (paymentInfo.getPaymentStatus() != PaymentStatusEnum.UNPAID.getStatus()){
            System.out.println("并非正在支付状态！！");
            return;
        }
        // 更新支付记录为已支付
            //设置支付记录参数
        PaymentInfo paymentInfoUpdate = new PaymentInfo();
        //设置状态为已支付
        paymentInfoUpdate.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        // 设置交易编号
        paymentInfoUpdate.setTradeNo(resultMap.get("transaction_id"));
        // 设置回调时间为当前系统时间
        paymentInfoUpdate.setCallbackTime(new Date());
        // ???
        paymentInfoUpdate.setCallbackContent(resultMap.toString());
        //更新
        this.updatePaymentInfo(out_trade_no,paymentInfoUpdate);

        // 修改订单状态
        //获得订单信息
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        //修改订单状态
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        //调用医院接口，通知更新支付状态
        //根据订单信息中的医院编号获得签名
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo){
            System.out.println("签名获取失败");
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String,Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
        if (result.getInteger("code") != 200){
            System.out.println("请求医院接口失败！");
            throw new YyghException(result.getString("message"),ResultCodeEnum.FAIL.getCode());
        }

    }

    /**
     * 获取支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id",orderId);
        wrapper.eq("payment_type",paymentType);

        return this.getOne(wrapper);
    }

    /**
     * 更新支付记录
     * @param out_trade_no
     * @param paymentInfoUpdate
     */
    private void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpdate) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",out_trade_no);
        this.update(paymentInfoUpdate,wrapper);
    }

    /**
     * 根据 对外业务编号 和支付类型查询支付记录
     * @param out_trade_no
     * @param paymentType
     * @return
     */
    private PaymentInfo getPaymentInfo(String out_trade_no, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no",out_trade_no);
        wrapper.eq("payment_type",paymentType);
        return this.getOne(wrapper);
    }
}
