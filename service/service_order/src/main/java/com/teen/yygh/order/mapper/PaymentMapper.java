package com.teen.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teen.yygh.model.order.PaymentInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author teen
 * @create 2022/4/8 23:20
 */
@Mapper
public interface PaymentMapper extends BaseMapper<PaymentInfo> {
}
