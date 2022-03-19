package com.teen.yygh.hosp.repository;

import com.teen.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author teen
 * @create 2022/3/18 20:25
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
