package com.teen.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.hosp.repository.ScheduleRepository;
import com.teen.yygh.hosp.service.DepartmentService;
import com.teen.yygh.hosp.service.HospitalService;
import com.teen.yygh.hosp.service.ScheduleService;
import com.teen.yygh.model.hosp.BookingRule;
import com.teen.yygh.model.hosp.Department;
import com.teen.yygh.model.hosp.Hospital;
import com.teen.yygh.model.hosp.Schedule;
import com.teen.yygh.vo.hosp.BookingScheduleRuleVo;
import com.teen.yygh.vo.hosp.ScheduleOrderVo;
import com.teen.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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
                //统记号源数量
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

    /**
     * 获取排班可预约的日期数据
     * 根据医院编号hoscode和科室编号depcode
     * 分页查询
     * @param page 页码
     * @param limit 每页记录数
     * @param hoscode 医院编号
     * @param depcode 科室编号
     * @return 带有详细信息的map集合
     */
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //用于装载结果
        Map<String, Object> result = new HashMap<>();
        //获取医院的详细信息（看来回头要花些时间看看数据库字段和实体字段）
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        //如果没有数据，则报错
        if (null == hospital){
            //数据异常
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //从查询出来的医院中获得预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期的分页对象
        IPage iPage = this.getListDate(page,limit,bookingRule);
        //从分页对象中获取当前页可预约的日期
        List<Date> dateList = iPage.getRecords();
        //获取可预约日期科室剩余预约数
        //创建条件构造器 根据医院编号、科室编号、工作日期作为where查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode)
                .and("workDate").in(dateList);
        //分组构造器 传入条件构造器
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate") //根据工作日期分组
                    .first("workDate").as("workDate")
                    .count().as("docCount")     //统计总记录数
                    .sum("availableNumber").as("availableNumber")  //将每个医生的剩余预约数相加即为总剩余预约数
                    .sum("reservedNumber").as("reservedNumber")     //将每个医生的最大预约数相加即为总预约数
        );
        //使用mongoTemplate进行查询，获得封装过的查询类对象                                                应查询的表(文档）对应的类型
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        //从查询类对象中得到对象列表
        List<BookingScheduleRuleVo> scheduleRuleVoList = aggregationResults.getMappedResults();
        //获取科室剩余预约数
        //转为Map，合并数据，将数据ScheduleVo根据“安排日期”合并到BookingRuleVo中
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        //如果查询到的对象列表不为空
        if (!CollectionUtils.isEmpty(scheduleRuleVoList)){
            //将list转为Map，方便根据日期查询医生                                        参数：key 是预约规则中的日期（不一定对应日期列表dateList）     value 是自身预约规则
            scheduleVoMap = scheduleRuleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约的排班规则
        //新建List集合用于存放预约规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //封装具体对象
        for (int i = 0; i < dateList.size(); i++) {
            //从日期列表中获得排班日期作为key
            Date date = dateList.get(i);
            //通过key来获取科室预约规则，不一定获取的到
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            //如果没有获取到，说明当天没有排班医生
            if (null == bookingScheduleRuleVo){
                //防止空指针，新建初始数据
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数设为0
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数设置为-1（无号）
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            //设置此对象的工作日期值为key
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date); //MM-dd 为的是方便前端显示
            //计算一下当前预约日期为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            //设置星期
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            //最后一页的最后一条记录即为即将预约 状态 0：正常 1：即将放号 -1：当天已停止放号
            if (i == dateList.size()-1 && iPage.getPages()==page){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约(第一条)如果过了停号时间 不能预约
            if (i == 0 && page == 1){
                //得到此医院预约规则中的停号时间
                DateTime stopTime = this.getDateTiem(new Date(),bookingRule.getStopTime());
                //如果停号时间比现在早（现在过了停号时间了）
                if (stopTime.isBeforeNow()){
                    //设置状态为停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            //将科室可预约规则存入结果list中
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //开始封装结果数据
        //放入可预约日期规则数据
        result.put("bookingScheduleList",bookingScheduleRuleVoList);
        //总记录数（2）
        result.put("total",iPage.getTotal());
        //其他基础数据使用map封装并装入 baseMap
        Map<String,String> baseMap = new HashMap<>();
        //医院名称，通过hoscode查询
        baseMap.put("hosname",hospitalService.getHospitalNameByHoscode(hoscode));
        //根据医院编号和科室编号查询科室
        Department department = departmentService.getDepartment(hoscode,depcode);
        //开始封装baseMap
        //大科室名称
        baseMap.put("bigname",department.getBigname());
        //科室名称
        baseMap.put("depname",department.getDepname());
        //系统时间 年月
        baseMap.put("workDateString",new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime",bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime",bookingRule.getStopTime());
        //将baseMap放入结果集
        result.put("baseMap",baseMap);
        return result;
    }

    /**
     * 根据id查询排班信息
     * @param scheduleId
     * @return
     */
    @Override
    public Schedule getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //获取排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if (null == schedule){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(null == hospital) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        //从医院中取到预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        if(null == bookingRule) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //将获取的数据set到结果对象中
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospitalNameByHoscode(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepname(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        //退号截止天数（就诊前一条为-1，当天为0）
        Integer quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTiem(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(),
                bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTiem(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());
        //预约截止时间
        DateTime endTime = this.getDateTiem(new DateTime().plusDays(bookingRule.getCycle()).toDate(),
                bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());
        //当天停止挂号时间
        DateTime stopTime = this.getDateTiem(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());

        return scheduleOrderVo;
    }

    /**
     * 用户预约成功后修改排班
     * @param schedule
     */
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        //主键一致，视为更新
        scheduleRepository.save(schedule);

    }

    /**
     * 获取可预约日期的分页数据
     * 根据bookingRule预约规则
     * 分页查询
     * @param page
     * @param limit
     * @param bookingRule 预约规则
     * @return 返回mybatis-plus分页对象,其中装载Date类型
     */
    private IPage<Date> getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天放号时间
        DateTime releaseTime = this.getDateTiem(new Date(),bookingRule.getReleaseTime());

        //预约周期 (10)
        Integer cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期后一天即为即将放号时间，当然，周期要+1
        if (releaseTime.isBeforeNow())
            cycle += 1;
        List<Date> dateList = new ArrayList<>();
        for (int i = 0;i < cycle;i++){
            //计算当前预约日期 将每天日期都显示出来（交给前端也行吧）
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最大显示7天。多了要分页
        List<Date> pageDateList = new ArrayList<>();
        //找出具体的开始记录和结束记录
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        //如果不够，结束记录指向最后一个元素，即列表长度
        if (end > dateList.size())
            end = dateList.size();
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,7,dateList.size());
        iPage.setRecords(pageDateList);
        return iPage;
    }

    /**
     * 将Date日期(yyyy-MM-dd HH:mm)转换为DateTime
     * @param date
     * @param timeString
     * @return
     */
    private DateTime getDateTiem(Date date, String timeString) {
        //当前系统日期+放号时间点
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        //将yyyy-MM-dd HH:mm 转换为DateTime类型
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
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
