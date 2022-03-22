package com.teen.yygh.dict.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author teen
 * @create 2022/3/19 16:24
 */
@FeignClient("service-dict")
@Repository
public interface DictFeignClient {

    /**
     * 调用dict端的接口，根据dictCode和value查询数据字典名称
     * 注意：@PathVariable中必须指明参数
     * @param dictCode
     * @param value
     * @return
     */
    @GetMapping("/admin/dict/getDickName/{dictCode}/{value}")
    public String getDickName(@PathVariable("dictCode") String dictCode,
                              @PathVariable("value") String value);

    @GetMapping("/admin/dict/getDickName/{value}")
    public String getDictName(@PathVariable("value") String value);
}
