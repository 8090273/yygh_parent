package com.teen.yygh.hosp.service;

import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @author teen
 * @create 2022/3/19 9:38
 */
public interface ScheduleService {
    void save(Map<String, Object> paramMap);

    Page<Schedule> findPageSchedule(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void remove(String hoscode, String hosScheduleId);
}
