package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.hosp.repository.ScheduleRepository;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.hosp.BookingScheduleRuleVo;
import com.teen.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author teen
 * @create 2022/3/19 9:39
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;

    //为了更加方便，使用MongoTemplate
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

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

    /**
     * 分页查询出排班规则信息
     * 需要根据日期排序
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return 返回一个map集合，包括排班信息、日期、预约人数等
     */
    @Override
    public Map<String, Object> getScheduleRulePageByHoscodeAndDepcode(long page, long limit, String hoscode, String depcode) {
        //根据医院编号和科室编号查询
        //MongoTemplate中有一个封装条件对象
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //根据医生workDate分组
        //实现分组操作,用于聚合操作(注意别引错包） org.springframework.data.mongodb.core.aggregation.Aggregation;
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),  //传入封装好的条件
                Aggregation.group("workDate") //分组字段
                .first("workDate").as("workDate")  //对分组字段进行显示
                //统级号源数量
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                Aggregation.skip((page-1) * limit),
                Aggregation.limit(limit)
        );

        //调用方法 执行查询
        AggregationResults<BookingScheduleRuleVo> aggResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //从对象中获取数据
        List<BookingScheduleRuleVo> scheduleRuleVos = aggResult.getMappedResults();

        //分组查询总记录数
        Aggregation totalAgg = Aggregation.newAggregation(
                Aggregation.match(criteria),  //传入封装好的条件
                Aggregation.group("workDate") //分组字段
        );
        AggregationResults<BookingScheduleRuleVo> totalAggResult = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggResult.getMappedResults().size();

        //封装星期属性dayOfWeek
        for (BookingScheduleRuleVo scheduleRuleVo : scheduleRuleVos) {
            Date workDate = scheduleRuleVo.getWorkDate();
            //转为星期
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            scheduleRuleVo.setDayOfWeek(dayOfWeek);

        }
        //设置最终数据，完成返回
        HashMap<String, Object> result = new HashMap<>();
        result.put("scheduleRuleVos",scheduleRuleVos);
        result.put("total",total);
        //获取医院名称,需要用另一个service的方法
        String hospitalName = hospitalService.getHospitalNameByHoscode(hoscode);

        //将基础信息放入baseMap
        HashMap<Object, Object> baseMap = new HashMap<>();
        baseMap.put("hospitalName",hospitalName);
        result.put("baseMap",baseMap);

        return result;
    }

    /**
     * 查询科室排班详情
     * @param hoscode
     * @param depcode
     * @param workDate
     * @return
     */
    @Override
    public List<Schedule> getScheduleRuleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> list = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());
        //封装医院名称、科室名称、日期对应星期
        list.stream().forEach(item->{
            this.packageSchedule(item);
        });

        return list;
    }

    private void packageSchedule(Schedule schedule){
        //医院名称
        schedule.getParam().put("hosname",hospitalService.getHospitalNameByHoscode(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",departmentService.getDepname(schedule.getHoscode(),schedule.getDepcode()));
        //设置星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
    }

    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
