package com.teen.yygh.hosp.controller;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 排班相关接口
 * @author teen
 * @create 2022/3/24 17:20
 */
@Api(tags = "排班管理")
@RestController
//@CrossOrigin
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {
    @Autowired
    ScheduleService scheduleService;

    /**根据医院和科室编号分页查询排班信息
     *
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return 返回一个Map集合，包含日期、已预约人数、可预约人数等
     */
    @ApiOperation(value = "根据医院和科室编号分页查询排班信息")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable long page,
                                  @PathVariable long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode){
        //因为返回值需要包含日期、已预约人数、可预约人数等，所以返回Map集合更方便
        //根据当前页码、每页记录数、医院编号、科室编号来分页查询排班规则信息
        Map<String ,Object> scheduleMap = scheduleService.getScheduleRulePageByHoscodeAndDepcode(page,limit,hoscode,depcode);
        return Result.ok(scheduleMap);

    }


    /**
     * 根据医院编号、科室编号、预约时间来查询详情
     * @param hoscode
     * @param depcode
     * @param workDate
     * @return
     */
    @ApiOperation(value = "根据医院和科室编号、工作日期查询预约详情")
    @GetMapping("getScheduleRuleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleRuleDetail(@PathVariable String hoscode,
                                        @PathVariable String depcode,
                                        @PathVariable String workDate){
        List<Schedule> list = scheduleService.getScheduleRuleDetail(hoscode,depcode,workDate);

        return Result.ok(list);

    }


}
