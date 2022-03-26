package com.teen.yygh.hosp.controller;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**医院科室接口
 *
 * @author teen
 * @create 2022/3/23 18:38
 */
@Api(tags = "科室管理")
@RestController
@RequestMapping("/admin/hosp/department")
@CrossOrigin
public class DepartmentController {
    @Autowired
    DepartmentService departmentService;

    @ApiOperation(value = "查询医院的所有科室列表")
    @GetMapping("getDepartmentList/{hoscode}")
    public Result getDepartmentList(@PathVariable String hoscode){
        //科室可能还包含小科室，所以应该使用DepartmentVo，此对象中有属性children为子科室列表
        List<DepartmentVo> departmentList =  departmentService.findDepartmentTree(hoscode);

        return Result.ok(departmentList);
    }
}
