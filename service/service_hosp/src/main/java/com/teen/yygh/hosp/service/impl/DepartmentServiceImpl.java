package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.hosp.repository.DepartmentRepository;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/18 20:27
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    DepartmentRepository departmentRepository;

    /**
     * 保存科室信息
     * @param paramMap
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //需要转换成department对象，使用 JSONObject
        Department department = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Department.class);

        //判断是否已经存在此科室信息
        //主键（医院编号，科室编号）
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());

        if(null!=departmentExist){
            //有则更新
            department.setId(departmentExist.getId());
            department.setCreateTime(departmentExist.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else {
            //无则添加
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    /**
     * 分页查询科室信息
     * @param page
     * @param limit
     * @param departmentQueryVo
     * @return
     */
    @Override
    public Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");

        //page-1为第page页
        Pageable pageable = PageRequest.of(page - 1, limit,sort);

        Department department = new Department();

        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符串匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        //创建实例
        Example<Department> example = Example.of(department, matcher);
        Page<Department> pages = departmentRepository.findAll(example, pageable);
        return pages;
    }

    /**
     * 删除科室
     * @param hoscode
     * @param depcode
     */
    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(null != department) {
            //departmentRepository.delete(department);
            departmentRepository.deleteById(department.getId());
        }
    }
}
