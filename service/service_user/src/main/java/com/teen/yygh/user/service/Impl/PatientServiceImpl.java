package com.teen.yygh.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.yygh.dict.client.DictFeignClient;
import com.teen.yygh.enums.DictEnum;
import com.teen.yygh.model.user.Patient;
import com.teen.yygh.user.mapper.PatientMapper;
import com.teen.yygh.user.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author teen
 * @create 2022/4/2 16:54
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    /**
     * 通过userId获取就诊人列表
     * @param userId
     * @return
     */
    @Override
    public List<Patient> findAllByUserId(Long userId) {
        //通过user_id获取就诊人列表
        QueryWrapper<Patient> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<Patient> list = this.list(wrapper);
        //此时其中的信息还不完整，其中的身份证类型等还只是编号，应通过远程调用字典来查询出具体内容
        /*list.stream().forEach(item->{
            //对列表每个元素进行封装
            this.packPatient(item);
        });*/
        //Collection.forEach（）使用集合的迭代器（如果指定了一个），集合里元素的处理顺序是明确的。相反，Collection.stream（）.forEach（）的处理顺序是不明确的。
        //list.stream().forEach(this::packPatient);
        list.forEach(this::packPatient);
        return list;
    }

    /**
     * 通过id来得到具体就诊人（不是userId，因为一个userId可对应多个就诊人）
     * @param id
     * @return
     */
    @Override
    public Patient getPatientById(Long id) {
        //直接查询，数据并不完整，需要进一步完善（如果可多表连接直接多表连接，但现在不允许）
        Patient patient = this.getById(id);
        //包装并返回对象
        return this.packPatient(patient);

    }

    /**
     * 使用远程调用dict服务 封装其他参数对象
     * @param patient
     */
    private Patient packPatient(Patient patient) {
        //DictEnum.CERTIFICATES_TYPE.getDictCode() = CertificatesType
        String certificatesTypeString = dictFeignClient.getDickName(DictEnum.CERTIFICATES_TYPE.getDictCode(), patient.getCertificatesType());
        //联系人证件
        //联系人证件类型
        String contactsCertificatesTypeString =
                dictFeignClient.getDickName(DictEnum.CERTIFICATES_TYPE.getDictCode(),patient.getContactsCertificatesType());
        //省
        String provinceString = dictFeignClient.getDictName(patient.getProvinceCode());
        //市
        String cityString = dictFeignClient.getDictName(patient.getCityCode());
        //区
        String districtString = dictFeignClient.getDictName(patient.getDistrictCode());

        patient.getParam().put("certificatesTypeString", certificatesTypeString);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;

    }
}
