package com.teen.yygh.hosp.service;

import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.hosp.ScheduleOrderVo;
import com.teen.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/19 9:38
 */
public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getScheduleRulePageByHoscodeAndDepcode(long page, long limit, String hoscode, String depcode);

    List<Schedule> getScheduleRuleDetail(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleById(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //修改排班接口
    void update(Schedule schedule);
}
