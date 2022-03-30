package com.teen.yygh.hosp.controller.api;

import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.helper.HttpRequestHelper;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.common.utils.MD5;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.hosp.service.HospitalSetService;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.acl.RoleQueryVo;
import com.teen.yygh.vo.hosp.DepartmentQueryVo;
import com.teen.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 使用mongodb进行保存
 * @author teen
 * @create 2022/3/17 10:21
 */
@Api(tags = "医院信息接口，负责提供上传信息")
@RestController
@RequestMapping("/api/hosp")
//@CrossOrigin //跨域资源共享开启，允许跨域访问
public class ApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 删除排班信息
     * @param request
     * @return
     */
    @PostMapping("schedule/remove")
    public Result remove(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //医院编号
        String hoscode = (String)paramMap.get("hoscode");
        //排班编号
        String hosScheduleId = (String)paramMap.get("hosScheduleId");

        hospitalSetService.verificationSign(paramMap);

        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

    /**
     * 分页查询排班信息接口
     * @param request
     * @return
     */
    @PostMapping("schedule/list")
    public Result findSchedule(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //医院编号
        String hoscode = (String)paramMap.get("hoscode");
        //科室编号
        String depcode = (String)paramMap.get("depcode");

        hospitalSetService.verificationSign(paramMap);
        //分页参数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel = scheduleService.findPageSchedule(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }

    /**
     * 上传排班接口
     * @param request
     * @return
     */
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //医院编号
        String hoscode = (String)paramMap.get("hoscode");
        hospitalSetService.verificationSign(paramMap);
        scheduleService.save(paramMap);
        return Result.ok();
    }

    /**
     * 删除科室接口
     * @param request
     * @return
     */
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //医院编号
        String hoscode = (String)paramMap.get("hoscode");
        //科室编号
        String depcode = (String) paramMap.get("depcode");
        //验签
        hospitalSetService.verificationSign(paramMap);
        departmentService.remove(hoscode,depcode);
        return Result.ok();

    }

    /**
     * 获取科室信息
     * @param request
     * @return
     */
    @PostMapping("department/list")
    public Result getDepartment(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //医院编号
        String hoscode = (String)paramMap.get("hoscode");
        //科室编号
        String depcode = (String) paramMap.get("depcode");
        //分页参数
        int page = StringUtils.isEmpty(paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int limit = StringUtils.isEmpty(paramMap.get("limit")) ? 10 : Integer.parseInt((String) paramMap.get("limit"));
        //验签
        hospitalSetService.verificationSign(paramMap);
        //因为要做分页查询，所以可以封装成一个vo实体类
        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);
        Page<Department> pageModel = departmentService.findPageDepartment(page,limit,departmentQueryVo);
        return Result.ok(pageModel);

    }

    /**
     * 上传科室接口
     *
     * @param request
     * @return
     */
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //验签
        hospitalSetService.verificationSign(paramMap);
        departmentService.save(paramMap);
        return Result.ok();

    }


    /**
     * 查询医院接口
     * @param request
     * @return
     */
    @PostMapping("hospital/show")
    public Result getHosp(HttpServletRequest request){
        //将参数转换
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestParameterMap);
        //血妈坑壁！！！！艹！
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String)paramMap.get("logoData");
        if(!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll(" ", "+");
            paramMap.put("logoData", logoData);
        }
        //验签
        hospitalSetService.verificationSign(paramMap);
        //取出hoscode并根据hoscoed查询
        String hoscode = (String)paramMap.get("hoscode");
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }


    /**
     * 上传医院接口
     * @param request
     * @return
     */
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request){
        //获取传过来的医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        //将map的value类型从字符串数组改为object，使用自行封装的工具类
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //血妈坑壁！！！！艹！
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoDataString = (String)paramMap.get("logoData");
        if(!StringUtils.isEmpty(logoDataString)) {
            String logoData = logoDataString.replaceAll(" ", "+");
            paramMap.put("logoData", logoData);
        }
        //校验签名
        hospitalSetService.verificationSign(paramMap);

        //调用service方法保存信息
        hospitalService.save(paramMap);
        return Result.ok();

    }
}
