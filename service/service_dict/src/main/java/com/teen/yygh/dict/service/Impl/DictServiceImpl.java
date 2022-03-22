package com.teen.yygh.dict.service.Impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teen.yygh.dict.listener.DictListener;
import com.teen.yygh.dict.mapper.DictMapper;
import com.teen.yygh.dict.service.DictService;
import com.teen.yygh.model.cmn.Dict;
import com.teen.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author teen
 * @create 2022/3/15 16:45
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService  {

    @Autowired
    DictMapper dictMapper;

    //根据id查询数据
    @Override
    @Cacheable(value = "dict",keyGenerator="keyGenerator")
    public List<Dict> getChildData(Long id) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("parent_id",id);
        List<Dict> list = baseMapper.selectList(wrapper);
        //为每个dict对象的hasChild属性赋值
        for (Dict dict: list) {
            dict.setHasChildren(hasChild(dict.getId()));
        }
        System.out.println("查询了数据库！");
        return list;
    }

    //还要判断子节点是否有子节点
    public boolean hasChild(Long id){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }

    //导出数据字典的数据

    @Override
    public void exportDictData(HttpServletResponse response) throws IOException {
        //设置下载信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("数据字典","utf-8");
        //以下载方式打开
        response.setHeader("Content-disposition","attachment;filename="+fileName+".xlsx");
        //查询数据库
        List<Dict> dicts = baseMapper.selectList(null);
        //对dict对象进行封装，深拷贝
        List<DictEeVo> dictEeVos = new ArrayList<>();
        for (Dict dict:
             dicts) {
            DictEeVo dictEeVo = new DictEeVo();
            //使用工具类进行深拷贝
            BeanUtils.copyProperties(dict,dictEeVo);
            dictEeVos.add(dictEeVo);
        }
        //进行表格写操作
        EasyExcel.write(response.getOutputStream(),DictEeVo.class).sheet("数据字典")
                .doWrite(dictEeVos);

    }

    //导入表格数据到数据字典

    @Override
    @CacheEvict(value = "dict",allEntries = true) //每次更新都会清空缓存
    public void importDict(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(dictMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("刷新了缓存");
    }

    //根据dictCode和value查询数据字典名称
    @Override
    public String getDictName(String dictCode, String value) {
        //如果dictCode为空，只根据value查询
        if(StringUtils.isEmpty(dictCode)){
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        }else {
            //select name from dict where value = 1 and parent_id = (select id from dict where dict_code)
            Dict codeDict = this.getDictByDictCode(dictCode);
            Long parentId = codeDict.getId();
            //根据parentId和value查询
            //注意：.eq方法中的字段必须对应数据库中的字段，驼峰不会被解析
            Dict finalDict = dictMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id",parentId)
                    .eq("value",value));
            return finalDict.getName();
        }
    }

    @Override
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //如果value能唯一定位数据字典，parentDictCode可以传空，例如：省市区的value值能够唯一确定
        if(StringUtils.isEmpty(parentDictCode)) {
            Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        } else {
            Dict parentDict = this.getDictByDictCode(parentDictCode);
            if(null == parentDict) return "";
            Dict dict = dictMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id", parentDict.getId()).eq("value", value));
            if(null != dict) {
                return dict.getName();
            }
        }
        return "";
    }

    /**
     * 根据DictCode查询第一层级的id，再根据id查询其子节点
     * @param dictCode
     * @return
     */
    @Override
    public List<Dict> findChildByDictCode(String dictCode) {
        Dict codeDict = this.getDictByDictCode(dictCode);
        if(null == codeDict) return null;
        return this.getChildData(codeDict.getId());
    }


    /**
     * 根据dictCode查询Dict
     * @param dictCode
     * @return
     */
    private Dict getDictByDictCode(String dictCode){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("dict_code",dictCode);
        Dict dict = dictMapper.selectOne(wrapper);
        return dict;
    }
}
