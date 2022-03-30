package com.teen.yygh.dict.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.teen.yygh.common.result.Result;
import com.teen.yygh.dict.service.DictService;
import com.teen.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/dict")
//@CrossOrigin
public class DictController {
    @Autowired
    DictService dictService;

    /**
     * 根据dictCode来获取省id 86，
     * 再根据id 86联动查询出具体的省市
     * @param dictCode
     * @return
     */
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findChildByDictCode/{dictCode}")
    public Result<List<Dict>> findChildByDictCode(
            @ApiParam(name = "dictCode", value = "节点编码", required = true)
            @PathVariable String dictCode) {
        List<Dict> list = dictService.findChildByDictCode(dictCode);
        return Result.ok(list);
    }

    /**
     * 根据父结点的DictCode，获取当前节点的子节点（父结点的孙节点）的字典名字
     * @param parentDictCode
     * @param value
     * @return
     */
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @ApiParam(name = "parentDictCode", value = "上级编码", required = true)
            @PathVariable("parentDictCode") String parentDictCode,

            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }

    @ApiOperation(value = "获取数据字典名称")
    @ApiImplicitParam(name = "value", value = "值", required = true, dataType = "Long", paramType = "path")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue("", value);
    }

    /**
     * 根据id查询子节点的数据
     * @param id
     * @return
     */
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

    //根据dictCode和value查询
    @ApiOperation("根据dictCode和value查询字典名称")
    @GetMapping("getDickName/{dictCode}/{value}")
    public String getDickName(@PathVariable String dictCode,
                                  @PathVariable String value){
        String dictName = dictService.getDictName(dictCode,value);
        return dictName;
    }


    //根据value查询
    @ApiOperation("根据value查询字典名称")
    @GetMapping("getDickName/{value}")
    public String getDictName(@PathVariable String value){
        String dictName = dictService.getDictName("",value);
        return dictName;
    }

}
