package com.teen.yygh.dict.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teen.yygh.model.cmn.Dict;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author teen
 * @create 2022/3/15 16:42
 */
public interface DictService extends IService<Dict> {
    List<Dict> getChildData(Long id);

    void exportDictData(HttpServletResponse response) throws IOException;

}
