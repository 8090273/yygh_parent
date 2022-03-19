package com.teen.yygh.dict.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.dict.service.DictService;
import com.teen.yygh.model.cmn.Dict;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author teen
 * @create 2022/3/15 16:59
 */
@RestController
@RequestMapping("/admin/dict")
@CrossOrigin
public class DictController {
    @Autowired
    DictService dictService;

    @GetMapping("getChildData/{id}")
    @ApiOperation("根据id查询子节点数据")
    public Result getChildData(@PathVariable Long id){
        List<Dict> list = dictService.getChildData(id);
        return Result.ok(list);

    }

    @GetMapping("exportData")
    @ApiOperation("导出数据字典")
    public void exportData(HttpServletResponse response) throws IOException {
        dictService.exportDictData(response);
    }

    @PostMapping("importData")
    @ApiOperation("导入表格到数据字典")
    public Result importData(MultipartFile file){
        dictService.importDict(file);
        return Result.ok();

    }

}
