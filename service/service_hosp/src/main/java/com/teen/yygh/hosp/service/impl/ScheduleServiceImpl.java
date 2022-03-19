package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.hosp.repository.ScheduleRepository;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/19 9:39
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    /**
     * 上传排班业务
     * @param paramMap
     */
    @Override
    public void save(Map<String, Object> paramMap) {
        //先把map封装为对象
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(paramMap), Schedule.class);

        //查看是否存在，存在则更新，不存在则添加
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());
        if(null !=scheduleExist){
            //如果不为空，则存在，更新对象
            //必须设置id，不然会识别为新的数据
            schedule.setId(scheduleExist.getId());
            schedule.setCreateTime(scheduleExist.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }

    /**
     * 查询排班信息
     * 分页查询
     * @param page
     * @param limit
     * @param scheduleQueryVo
     * @return
     */
    @Override
    public Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo) {
        //查出的结果以创建时间排序
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //创建分页对象,参数为当前页码，每页记录数，排序规则
        Pageable pageable = PageRequest.of(page,limit,sort);
        //条件匹配器实例需要实体类型的对象，所以new一个
        Schedule schedule = new Schedule();
        //为实体类型的对象赋值，使用工具类直接copy,将前者的值copy给后者
        BeanUtils.copyProperties(scheduleQueryVo,schedule);
        //不要忘了设置状态和逻辑删除标志
        schedule.setIsDeleted(0); //0为未删除
        schedule.setStatus(1);  //1为可用

        //创建条件匹配器，设置限制条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)  //改变默认字符串匹配规则：模糊搜索
                                    .withIgnoreCase(true);  //忽视大小写
        //Example.of()传入实体类型schedule和条件匹配器(ExampleMather)，返回条件匹配器实例
        Example<Schedule> example = Example.of(schedule, matcher);
        //使用findAll方法分页查询，传入条件匹配器实例Example和分页对象
        Page<Schedule> pages = scheduleRepository.findAll(example,pageable);
        return pages;
    }

    /**
     * 删除排班
     * @param hoscode
     * @param hosScheduleId
     */
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        //先查询是否存在
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (null != schedule){
            scheduleRepository.deleteById(schedule.getId());
        }

    }
}
