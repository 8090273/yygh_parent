package com.teen.yygh.hosp.controller.api;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.hosp.service.HospitalSetService;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.hosp.DepartmentVo;
import com.teen.yygh.vo.hosp.HospitalQueryVo;
import com.teen.yygh.vo.hosp.ScheduleOrderVo;
import com.teen.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.bouncycastle.asn1.crmf.PKIPublicationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/26 22:31
 */
@Api(tags = "负责前台显示信息")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {
    @Autowired
    HospitalService hospitalService;

    @Autowired
    DepartmentService departmentService;

    //排班预约详情接口需要用到
    @Autowired
    private ScheduleService scheduleService;

    //医院设置service
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation("查询医院列表")
    @GetMapping("findHospitalList/{page}/{limit}")
    public Result findHospitalList(@PathVariable Integer page,
                                   @PathVariable Integer limit,
                                   HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectHospPage(page, limit, hospitalQueryVo);

        return Result.ok(hospitals);

    }

    @ApiOperation(value = "根据医院名称模糊查询")
    @GetMapping("findByHosName/{hosname}")
    public Result findByHosName(@PathVariable String hosname){
        List<Hospital> hospitalList = hospitalService.findByHosname(hosname);
        return Result.ok(hospitalList);
    }

    /**
     * 根据医院的编号查询科室信息
     * 难点
     * @param hoscode
     * @return
     */
    @ApiOperation(value = "根据医院编号查询科室信息")
    @GetMapping("department/{hoscode}")
    public Result department(@PathVariable String hoscode){
        List<DepartmentVo> departmentTree = departmentService.findDepartmentTree(hoscode);
        return Result.ok(departmentTree);
    }

    @ApiOperation(value = "查询医院预约详情信息")
    @GetMapping("findBookingRuleDetail/{hoscode}")
    public Result findBookingRuleDetail(@PathVariable String hoscode){
        Map<String,Object> bookingRuleDetailMap = hospitalService.findBookingRuleDetailByHoscode(hoscode);

        return Result.ok(bookingRuleDetailMap);
    }

    //获取可预约的排班数据
    @ApiOperation(value = "分页查询出可预约的排班信息数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingScheduleRule(@ApiParam(name = "page",value = "当前页码",required = true) @PathVariable Integer page,
                                         @ApiParam(name = "limit",value = "每页记录数",required = true) @PathVariable Integer limit,
                                         @ApiParam(name = "hoscode",value = "医院编号",required = true) @PathVariable String hoscode,
                                         @ApiParam(name = "depcode",value = "科室code",required = true) @PathVariable String depcode){
        Map<String,Object> map = scheduleService.getBookingScheduleRule(page,limit,hoscode,depcode);
        return Result.ok(map);
    }

    //获取排班数据
    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hoscode",value = "医院code",required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode",value = "科室code",required = true)
            @PathVariable String depcode,
            @ApiParam(name = "workDate",value = "排班日期",required = true)
            @PathVariable String workDate
    ){
        return Result.ok(scheduleService.getScheduleRuleDetail(hoscode,depcode,workDate));
    }

    //根据排班id获取排班信息
    @ApiOperation(value = "根据排班id获取排班信息")
    @GetMapping("getSchedule/{scheduleId}")
    public Result getScheduleById(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        return Result.ok(schedule);
    }

    //根据排班id获取预约下单数据
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }

    //获取医院签名信息
    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@ApiParam(name = "hoscode", value = "医院code", required = true)
                                        @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }

}
