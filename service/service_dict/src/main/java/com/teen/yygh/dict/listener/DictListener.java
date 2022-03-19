package com.teen.yygh.dict.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.teen.yygh.dict.mapper.DictMapper;
import com.teen.yygh.model.cmn.Dict;
import com.teen.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

/**
 * EasyExcel的监听表格的监听器
 * @author teen
 * @create 2022/3/16 6:28
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {
    //因为要进行增删改查，所以需要依赖注入mapper
    private DictMapper dictMapper;
    //使用有参构造注入依赖
    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    //一行一行的读取，从第二行开始
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dictMapper.insert(dict);

    }



    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
