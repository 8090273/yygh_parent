package com.teen.yygh.hosp.controller;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.vo.hosp.HospitalQueryVo;
import com.teen.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * @author teen
 * @create 2022/3/19 12:02
 */
@Api(tags = "医院管理")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;

    /**
     * 分页查询医院
     * @param page
     * @param limit
     * @param hospitalQueryVo
     * @return
     */
    @ApiOperation(value = "分页查询医院信息")
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable Integer page,
                           @PathVariable Integer limit,
                           HospitalQueryVo hospitalQueryVo){
        //条件查询带分页的查询都应经过QueryVo类进行封装

        //因为医院数据都存在mongodb中
        Page<Hospital> pageModel = hospitalService.selectHospPage(page,limit,hospitalQueryVo);

        return Result.ok(pageModel);

    }


    /**
     * 改变医院的上线状态
     * @param id 通过id查询具体医院
     * @param status 应修改的医院状态
     * @return
     */
    @ApiOperation(value = "改变医院上线状态")
    @GetMapping("updateHospitalStatus/{id}/{status}")
    public Result updateHospitalStatus(@PathVariable String id,
                                       @PathVariable Integer status){
        hospitalService.updateStatus(id,status);
        return Result.ok();
    }

    /**
     * 根据id查询医院的具体信息
     * @param id
     * @return 返回HashMap集合 key： "hospital" "bookingRule"
     */
    @ApiOperation(value = "查询医院详情信息")
    @GetMapping("showHospitalDetails/{id}")
    public Result showHospitalDetails(@PathVariable String id){
        //返回的是一个HashMap集合，包含hospital和bookingRule
        return Result.ok(hospitalService.getHospitalById(id));
    }
}
