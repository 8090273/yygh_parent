package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.hosp.repository.DepartmentRepository;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.vo.hosp.DepartmentQueryVo;
import com.teen.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * 根据hoscode查询此医院下所有科室
     * 难点
     * @param hoscode
     * @return
     */
    @Override
    public List<DepartmentVo> findDepartmentTree(String hoscode) {
        List<DepartmentVo> result = new ArrayList<>();
        //根据医院编号查询医院所有科室信息
        Department departmentQuery = new Department();
        departmentQuery.setHoscode(hoscode);
        //创建mongoDB中的example
        Example<Department> example = Example.of(departmentQuery);
        //数据库中所有科室的列表信息
        List<Department> departmentDbList = departmentRepository.findAll(example);

        //根据大科室的编号 bigcode 分组， 获取每个大科室里面下级子科室
        //使用java8新特性stream流 的分组
        Stream<Department> departmentStream = departmentDbList.stream(); //将集合变为流,基于流的计算并不改变原数据结构
        //对流做筛选分组 ,返回Map集合，key是科室编号，List是大科室下的小科室信息
        Map<String, List<Department>> departmentMap = departmentStream.collect(Collectors.groupingBy(Department::getBigcode));
        //同等写法
        /*Map<String,List<Department>> map = new HashMap<>();
        for (Department department : departmentList) {
            if (map.get(department.getHoscode()).isEmpty()){
                List list = new ArrayList();
                list.add(department);
                map.put(department.getHoscode(),list);
            }else {
                map.get(department.getHoscode()).add(department);
            }
        }*/

        //封装为DepartmentVo以便返回
        //遍历Map,得到Map的entry关系 然后entry.getKey()  entry.getValue()
        for (Map.Entry<String,List<Department>> entry : departmentMap.entrySet() ){
            String bigCode = entry.getKey();

            List<Department> departmentList = entry.getValue();

            //封装大科室
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            bigDepartmentVo.setDepcode(bigCode);
            //大科室的名字是ArrayList中随便一个科室的大科室名
            bigDepartmentVo.setDepname(departmentList.get(0).getBigname());

            //封装子科室  子科室没有子科室了
            List<DepartmentVo> childrenDepartmentVoList = new ArrayList<>();
            for (Department department : departmentList){
                DepartmentVo childrenDepartmentVo = new DepartmentVo();
                childrenDepartmentVo.setDepcode(department.getDepcode());
                childrenDepartmentVo.setDepname(department.getDepname());
                //将子科室一个个放入list中
                childrenDepartmentVoList.add(childrenDepartmentVo);
            }
            //将大科室的children属性指向list
            bigDepartmentVo.setChildren(childrenDepartmentVoList);
            //将大科室一个个放入结果list中
            result.add(bigDepartmentVo);
        }
        //返回结果list
        return result;
    }

    @Override
    public String getDepname(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);

        if (department!=null){
            return department.getDepname();
        }
        return null;
    }
}
