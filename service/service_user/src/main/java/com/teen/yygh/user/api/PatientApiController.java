package com.teen.yygh.user.api;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.common.util.AuthContextHolder;
import com.teen.yygh.model.user.Patient;
import com.teen.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author teen
 * @create 2022/4/2 16:52
 */
@Api(tags = "就诊人接口")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {
    @Autowired
    private PatientService patientService;
    //获取就诊人列表
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request){
        //从request中获取token，通过token获取用户id
        Long userId = AuthContextHolder.getUserId(request);
        //通过用户id获取所有就诊人
        List<Patient> list = patientService.findAllByUserId(userId);

        return Result.ok(list);
    }

    //添加就诊人
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    //根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }
    //修改就诊人
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }
    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public Result removePatient(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }
}
