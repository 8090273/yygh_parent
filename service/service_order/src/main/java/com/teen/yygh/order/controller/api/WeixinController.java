package com.teen.yygh.order.controller.api;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.enums.PaymentTypeEnum;
import com.teen.yygh.order.service.PaymentService;
import com.teen.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author teen
 * @create 2022/4/8 23:07
 */
@RestController
@RequestMapping("api/order/weixin")
public class WeixinController {
    @Autowired
    private WeixinService weixinService;

    @Autowired
    private PaymentService paymentService;

    //生成微信二维码接口
    @ApiOperation(value = "生成微信二维码")
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId){
        Map map = weixinService.createNative(orderId);
        return Result.ok(map);
    }

    //查询支付状态（前端轮询）
    @ApiOperation(value = "查询支付状态")
    @GetMapping("queryPayStatus/{orderId}")
    public Result queryPayStatus(@ApiParam(name = "orderId", value = "订单id", required = true) @PathVariable Long orderId){
        //调用查询接口
        Map<String,String> resultMap = weixinService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.name());
        if (null == resultMap){
            System.out.println("支付出错！");
            return Result.fail().message("支付出错");
        }
        if ("SUCCESS".equals(resultMap.get("trade_state"))){
            //如果成功
            //更改订单状态，处理支付结果
            String out_trade_no = resultMap.get("out_trade_no");
            paymentService.paySuccess(out_trade_no,PaymentTypeEnum.WEIXIN.getStatus(),resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }

}
