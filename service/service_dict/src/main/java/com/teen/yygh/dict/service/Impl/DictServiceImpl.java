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
}
