package com.teen.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.common.rabbitMq.constant.MqConst;
import com.teen.common.rabbitMq.service.RabbitMqService;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.HttpRequestHelper;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.enums.OrderStatusEnum;
import com.teen.yygh.hosp.client.HospitalFeignClient;
import com.teen.yygh.model.order.OrderInfo;
import com.teen.yygh.model.user.Patient;
import com.teen.yygh.order.mapper.OrderMapper;
import com.teen.yygh.order.service.OrderService;
import com.teen.yygh.order.service.WeixinService;
import com.teen.yygh.user.client.PatientFeignClient;
import com.teen.yygh.vo.hosp.ScheduleOrderVo;
import com.teen.yygh.vo.msm.MsmVo;
import com.teen.yygh.vo.order.OrderMqVo;
import com.teen.yygh.vo.order.OrderQueryVo;
import com.teen.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.xml.ws.spi.http.HttpHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author teen
 * @create 2022/4/6 16:45
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {
    //调用user模块的就诊人接口
    @Autowired
    private PatientFeignClient patientFeignClient;

    //调用hosp模块的排班信息接口
    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    //调用rabbitMQ发送信息
    @Autowired
    private RabbitMqService rabbitMqService;

    //调用微信支付接口进行退款
    @Autowired
    private WeixinService weixinService;

    /**
     * 生成订单
     * @param scheduleId
     * @param patientId
     * @return
     */
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //获取就诊人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        if(null == patient) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if(null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //如果当前时间不可以预约了，直接抛出异常
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            //时间已过
//            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
        //获取签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if(null == scheduleOrderVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //如果可预约数不足
        if(scheduleOrderVo.getAvailableNumber() <= 0) {
            //可预约数不足
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        //添加到订单表中(听弹幕说最好加个redis锁，我觉得有道理）
        OrderInfo orderInfo = new OrderInfo();
        //将scheduleOrderVo数据复制到orderInfo中
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);    //此方法复制的不全
        //补全其他参数
        //订单交易号 当前时间戳+随机数
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        //订单状态：预约成功，待支付
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);

        //调用医院接口（hospital-manage微服务)，实现预约挂号操作
        //设置调用医院接口需要的参数,将参数放到map集合中去
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        //安排日期
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        //医事服务费
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        //证件类型
        paramMap.put("certificatesType",patient.getCertificatesType());
        //证件编号
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        //省code
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        //区code
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        //联系人证件类型
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        //联系人手机
        paramMap.put("contactsPhone",patient.getContactsPhone());
        //获取时间戳
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        //从请求中获取签名
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        //签名
        paramMap.put("sign", sign);

        //请求医院系统接口 获得json数据
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl() + "/order/submitOrder");

        //如果请求成功
        if (result.getInteger("code") == 200){
            //从json中得到data数据
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            this.updateById(orderInfo);
            System.out.println("订单生成成功！\n订单内容是："+orderInfo.toString());
            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //发送mq信息更新号源和短信通知
            //发送mq进行号源更新

            //新建消息实体
            OrderMqVo orderMqVo = new OrderMqVo();
            //设置排班id
            orderMqVo.setScheduleId(scheduleId);
            //剩余预约数
            orderMqVo.setAvailableNumber(availableNumber);
            //可预约数
            orderMqVo.setReservedNumber(reservedNumber);

            //短信提示
            MsmVo msmVo = new MsmVo();
            //设置短信参数,只设置手机号就可以了，其他没法发送
            msmVo.setPhone(orderInfo.getPatientPhone());
            orderMqVo.setMsmVo(msmVo);
            // TODO 发送预约成功短信
            System.out.println("------------开始发送MQ短信---------: ");
            rabbitMqService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);

        }else {
            //抛出失败信息
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

        System.out.println("订单id是：" + orderInfo.getId());
        return orderInfo.getId();
    }

    /**
     * 分页查询订单
     * @param pageParam
     * @param orderQueryVo
     * @return
     */
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //从vo对象中获取查询条件
        String name = orderQueryVo.getKeyword(); // 医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人id
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        //当条件非空时 设置条件
        if (!StringUtils.isEmpty(name)){
            wrapper.like("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)){
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }

        //调用mapper方法
        Page<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        pages.getRecords().stream().forEach(item->{
            this.packOrderInfo(item);
        });

        return pages;
    }

    /**
     * 根据id获取订单详情
     * @param orderId
     * @return
     */
    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = this.getById(orderId);
        //参数不全，还要封装一下
        this.packOrderInfo(orderInfo);
        return orderInfo;
    }

    /**
     * 根据id获取订单详情
     * @param id
     * @return
     */
    @Override
    public Map<String, Object> show(Long id) {
        Map<String, Object> map = new HashMap<>();
        OrderInfo orderInfo = this.packOrderInfo(this.getById(id));
        map.put("orderInfo",orderInfo);
        Patient patient = patientFeignClient.getPatientOrder(orderInfo.getPatientId());
        map.put("patient",patient);
        return map;
    }

    /**
     * 取消订单
     * @param orderId
     * @return
     */
    @Override
    public Boolean cancelOrder(Long orderId) {
        OrderInfo orderInfo = this.getById(orderId);
        // 当前时间大于退号时间，则不能取消预约
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()){
            System.out.println("已过退号时间！");
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (null == signInfoVo){
            System.out.println("获取签名失败");
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        HashMap<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        //预约记录唯一标识（医院预约记录主键
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updateCancelStatus");
        if (result.getInteger("code") != 200){
            System.out.println("请求失败");
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }else {
            //是否已经支付？
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus()){
                //已支付，进行退款
                Boolean isRefund = weixinService.refund(orderId);
                if (!isRefund){
                    //如果退款失败
                    System.out.println("orderServiceImpl.cancelOrder:取消订单失败！");
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
            }
            // 未支付或退款成功则直接进行更改订单状态
            //设置订单状态为 取消预约
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            this.updateById(orderInfo);
            System.out.println("取消预约成功！！！");

            //预约取消后发送mq信息更新预约数 可以与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接受的可预约数-1即可
            OrderMqVo orderMqVo = new OrderMqVo();
            //排班信息
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //短信内容
            MsmVo msmVo = new MsmVo();
            //只设置手机号即可
            msmVo.setPhone(orderInfo.getPatientPhone());
            orderMqVo.setMsmVo(msmVo);
            //发送mq消息
            System.out.println("开始发送取消预约的MQ消息给MQ！！！");
            rabbitMqService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);
        }
        return true;
    }

    /**
     * 封装订单对象
     * @param orderInfo
     */
    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

}
