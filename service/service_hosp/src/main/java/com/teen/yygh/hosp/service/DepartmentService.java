package com.teen.yygh.hosp.service;

import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.vo.hosp.DepartmentQueryVo;
import com.teen.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/18 20:27
 */
public interface DepartmentService {
    void save(Map<String, Object> paramMap);

    Page<Department> findPageDepartment(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> findDepartmentTree(String hoscode);

    String getDepname(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}
