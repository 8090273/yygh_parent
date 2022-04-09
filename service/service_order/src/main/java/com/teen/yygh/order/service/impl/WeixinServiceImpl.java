package com.teen.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.teen.yygh.enums.PaymentTypeEnum;
import com.teen.yygh.enums.RefundStatusEnum;
import com.teen.yygh.model.order.OrderInfo;
import com.teen.yygh.model.order.PaymentInfo;
import com.teen.yygh.model.order.RefundInfo;
import com.teen.yygh.order.service.OrderService;
import com.teen.yygh.order.service.RefundInfoService;
import com.teen.yygh.order.service.WeixinService;
import com.teen.yygh.order.util.ConstantPropertiesUtils;
import com.teen.yygh.order.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.swing.event.CaretListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author teen
 * @create 2022/4/8 23:09
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentServiceImpl paymentService;

    //退款service
    @Autowired
    private RefundInfoService refundInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 生成微信支付二维码
     * @param orderId
     * @return
     */
    @Override
    public Map createNative(Long orderId) {

        //此处try-catch是WXPayUtil.generateSignedXml
        try {
            //从redis中获得有时效性的二维码
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            //如果获取到了
            if (payMap != null){
                System.out.println("redis中已有支付二维码！！！"+payMap);
                return payMap;
            }

            // 得到订单详情
            OrderInfo order = orderService.getById(orderId);

            //向支付记录表加数据
            paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());

            // 封装订单信息

            //设置参数
            Map paramMap = new HashMap();
            //商户公众号id
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            //商户号
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            //生成唯一的字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //就诊信息
            String body = order.getReserveDate() + "就诊"+ order.getDepname();
            paramMap.put("body", body);
            //订单编码
            paramMap.put("out_trade_no", order.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            //订单金额 为了测试，写为0.01元
            paramMap.put("total_fee", "1");
            //ip地址
            paramMap.put("spbill_create_ip", "127.0.0.1");
            //回调地址 暂时用不到
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            //支付类型  NATIVE：扫描支付
            paramMap.put("trade_type", "NATIVE");

            //请求微信接口生成二维码
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //封装为XML格式
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            //设为支持https
            httpClient.setHttps(true);
            httpClient.post();

            //获取微信接口的响应
            String xml = httpClient.getContent();
            //将xml转为map集合
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            System.out.println("微信响应的resultMap为："+resultMap);
            //封装其他参数到map中
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            //订单金额
            map.put("totalFee", order.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            //二维码地址
            map.put("codeUrl", resultMap.get("code_url"));

            //放入redis中，让其有时效性
            //如果返回结果不为空，说明请求接口成功了
            if (resultMap.get("result_code") != null){
                //以订单id为key map集合为值， 有效期120分钟
                redisTemplate.opsForValue().set(orderId.toString(),map,120, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("！！！！！！！！！！！！请求微信接口异常！！！！！！！！！！");
        return null;
    }

    /**
     * 查询支付状态
     * @param orderId
     * @param name
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(Long orderId, String name) {
        //根据订单id得到具体订单数据
        OrderInfo orderInfo = orderService.getById(orderId);

        //封装请求参数
        Map paramMap = new HashMap<>();
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            //发送请求
            client.post();

            //接收响应
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 微信退款 根据订单id
     * @param orderId
     * @return
     */
    @Override
    public Boolean refund(Long orderId) {
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfoQuery);
        // 如果退款记录的状态为 已退款
        if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
            System.out.println("已退款！");
            return true;
        }
        //设置请求属性
        Map<String,String> paramMap = new HashMap<>(8);
        paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
        paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id",paymentInfoQuery.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no",paymentInfoQuery.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no","tk"+paymentInfoQuery.getOutTradeNo()); //商户退款单号
        paramMap.put("total_fee","1");
        paramMap.put("refund_fee","1");
        String paramXml = null;
        try {
            paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))){
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                // 设置退款状态：已退款
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                // 更新退款记录
                refundInfoService.updateById(refundInfo);
                return true;

            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }
}
