package com.teen.yygh.hosp.repository;

import com.teen.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 配置mongodb的Repository
 * @author teen
 * @create 2022/3/17 10:10
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {
    //根据hascode查询出医院信息
    Hospital getHospitalByHoscode(String hoscode);

    List<Hospital> findHospitalByHosnameLike(String hosname);
}
