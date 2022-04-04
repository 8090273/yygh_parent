package com.teen.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teen.yygh.model.user.Patient;

import java.util.List;

/**
 * @author teen
 * @create 2022/4/2 16:54
 */
public interface PatientService extends IService<Patient> {
    List<Patient> findAllByUserId(Long userId);

    Patient getPatientById(Long id);
}
