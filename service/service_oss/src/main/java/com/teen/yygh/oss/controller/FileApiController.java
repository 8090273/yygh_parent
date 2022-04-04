package com.teen.yygh.oss.controller;

import com.teen.yygh.common.result.Result;
import com.teen.yygh.oss.serivec.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author teen
 * @create 2022/4/1 8:56
 */
@Api(tags = "上传文件接口")
@RestController
@RequestMapping("/api/oss/file")
public class FileApiController {

    @Autowired
    private FileService fileService;

    //上传文件到阿里云
    @ApiOperation(value = "上传文件到oss,返回上传路径")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file){  //SpringMVC 通过MultipartFile 参数获取上传来的文件
        //获取文件 返回上传后的oss路径
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
