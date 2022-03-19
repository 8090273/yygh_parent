package com.teen.yygh.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teen.yygh.model.cmn.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;

/**
 * @author teen
 * @create 2022/3/15 16:38
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}
