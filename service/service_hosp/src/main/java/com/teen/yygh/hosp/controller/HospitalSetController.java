package com.teen.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.hosp.mapper.HospitalSetMapper;
import com.teen.yygh.model.hosp.HospitalSet;
import com.teen.yygh.hosp.service.HospitalSetService;
import com.teen.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * @author teen
 * @create 2022/3/7 16:58
 */
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("admin/hosp/hospitalSet")
//@CrossOrigin //跨域资源共享开启，允许跨域访问
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    //查询医院设置表中的所有信息

    /**
     * 查询所有医院的信息
     * @return
     */
    @ApiOperation(value="获取医院设置所有信息")
    @GetMapping("findAll")
    public Result findAllHospitalSet(){

//        System.out.println("测试成功");

        //调用service的方法
        List<HospitalSet> list = hospitalSetService.list();

        return Result.ok(list);
    }

    /**
     * 根据id删除医院设置，
     * 逻辑删除，不删除物理信息，只对记录进行标记
     * @param id 医院id
     * @return
     */
    @ApiOperation(value="逻辑删除医院设置信息")
    @DeleteMapping("deleteById/{id}")
    public Result removeHospSet(@PathVariable Long id){
        //根据id逻辑删除医院信息
        boolean flag = hospitalSetService.removeById(id);
        if (flag){
            return Result.ok();
        }
        return Result.fail();
    }

    /**
     * 条件查询带分页
     * @param current 当前页码
     * @param limit 每页条数
     * @param hospitalQueryVo 医院vo类
     * @return
     */
    @ApiOperation(value = "条件查询带分页查询医院设置")
    @PostMapping("findHospitalSet/{current}/{limit}")
    public Result findHospSet(@PathVariable Long current,
                              @PathVariable Long limit,
                              @RequestBody(required = false) HospitalQueryVo hospitalQueryVo){
        //构造分页对象
        Page<HospitalSet> page= new Page<>(current,limit);
        //构造条件
        QueryWrapper wrapper = new QueryWrapper();
        String hosname = hospitalQueryVo.getHosname();
        String hoscode = hospitalQueryVo.getHoscode();
        if (hosname!=null)
            wrapper.like("hosname",hospitalQueryVo.getHosname());
        if (hoscode!=null)
            wrapper.eq("hoscode",hospitalQueryVo.getHoscode());
        //调用方法实现分页查询
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);

        return Result.ok(hospitalSetPage);
    }

    //添加医院设置
    /**
     * 添加医院信息，其中status状态、SignKey签名、创建时间、id等不需要传入
     * @param hospitalSet JSON格式的对象
     * @return ok或fail
     */
    @ApiOperation("添加一个医院设置的信息")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //需要手动设置两个属性：密钥和状态
        //设置status属性:1可用0不可用
        hospitalSet.setStatus(1);
        //设置密钥，使用MD5加密生成的随机数
        //使用随机数生成器
        Random random = new Random();
        //密钥内容为系统时间+随机数
        hospitalSet.setSignKey(DigestUtils.md5DigestAsHex((System.currentTimeMillis()+random.nextInt(1000)+"").getBytes(StandardCharsets.UTF_8)));

        boolean save = hospitalSetService.save(hospitalSet);
        if (save){
            return Result.ok();
        }
        return Result.fail();


    }

    /**
     * 根据id获取医院设置
     * @param id 医院id
     * @return 医院对象
     */
    @ApiOperation("根据id获取医院设置")
    @GetMapping("getHospitalSet/{id}")
    public Result getHospitalSet(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    /**
     * 修改医院设置
     * @param hospitalSet 医院JSON，只有一个
     * @return
     */
    @ApiOperation("修改医院设置信息")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet){
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if (flag)
            return Result.ok();
        return Result.fail();

    }


    /**
     * 批量删除医院
     * @param idList 元素为id的List集合
     * @return 返回ok
     */
    @ApiOperation("批量删除医院设置，传入id列表")
    @DeleteMapping("batchRemove")
    public Result batchRemoveHospitalSet(@RequestBody List<Long> idList){
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }


    /**
     * 修改医院的锁定与解锁状态
     * 只有医院的状态为解锁状态，我们才能进行和医院系统的对接,实现数据的操作；
     * 如果把医院的状态锁定，则不能进行对接
     * @param id 医院id
     * @param status 要修改的状态 1为解锁 0为锁定
     * @return
     */
    @ApiOperation("修改医院的锁定状态")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer status){
        //先根据id查询出来实体类
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //修改状态
        hospitalSet.setStatus(status);
        //写回数据库
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();

    }


    /**
     * 发送签名密钥
     *
     * 我们通过医院的接口和我们平台进行对接，双方要约定好一个密钥。
     * 在添加医院的接口中我们手动设置了每一个医院的密钥，
     * 我们要把密钥发送给医院的接口，让它对其进行配置，
     * 完成一个相互的对接，所以我们要进行一个发送
     * @param id
     * @return
     */
    @ApiOperation("发送签名密钥")
    @PutMapping("sendKey/{id}")
    public Result sendHospitalKey(@PathVariable Long id){
        //根据id查到实体类
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        //得到实体类中的医院编号、签名密钥
        String hoscode = hospitalSet.getHoscode();
        String signKey = hospitalSet.getSignKey();

        // TODO 发送短信

        return Result.ok();

    }
}
