package com.teen.yygh.order.controller.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.common.util.AuthContextHolder;
import com.teen.yygh.enums.OrderStatusEnum;
import com.teen.yygh.model.order.OrderInfo;
import com.teen.yygh.order.service.OrderService;
import com.teen.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author teen
 * @create 2022/4/6 16:41
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("api/order/orderInfo")
public class OrderApiController {
    @Autowired
    OrderService orderService;

    //生成挂号订单
    @ApiOperation(value = "生成订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder( @ApiParam(name = "scheduleId", value = "排班id", required = true)
                               @PathVariable String scheduleId,
                               @ApiParam(name = "patientId", value = "就诊人id", required = true)
                               @PathVariable Long patientId){
        System.out.println("------------开始生成订单-----------");
        return Result.ok(orderService.saveOrder(scheduleId,patientId));
    }

    //查询订单列表
    @ApiOperation(value = "分页查询订单")
    @GetMapping("auth/{page}/{limit}")
    public Result list(@PathVariable Long page, @PathVariable Long limit, OrderQueryVo orderQueryVo, HttpServletRequest request){
        //获得当前用户id
        Long userId = AuthContextHolder.getUserId(request);
        //设置入查询订单对象
        orderQueryVo.setUserId(userId);
        //分页查询
        Page<OrderInfo> pageParam = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(pageParam,orderQueryVo);
        return Result.ok(pageModel);
    }

    //获取订单状态。订单状态我们是封装到枚举中的，页面搜索需要一个下拉列表展示，所以我们通过接口返回页面
    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    //获取订单详情
    //根据订单id查询订单详情
    @ApiOperation(value = "根据订单id查询订单详情")
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrders(@PathVariable String orderId) {
        OrderInfo orderInfo = orderService.getOrder(orderId);
        return Result.ok(orderInfo);
    }

    // 取消预约
    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@ApiParam(name = "orderId",value = "订单id",required = true) @PathVariable("orderId") Long orderId){
        return Result.ok(orderService.cancelOrder(orderId));
    }

}
