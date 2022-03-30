package com.teen.yygh.hosp.controller.api;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.vo.hosp.DepartmentVo;
import com.teen.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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
}
