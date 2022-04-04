package com.teen.yygh.msm.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author teen
 * @create 2022/3/30 17:39
 */
public class test01 {

    public static void main(String[] args) {
        String host = "https://miitangs10.market.alicloudapi.com";
        String path = "/v1/tools/sms/notify/sender";
        String method = "POST";
        String appcode = "d7dd18c99d5e409c81d675659eecfdd3";
//        String appcode = "556af59039b148d1acf655937417a456";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //需要给X-Ca-Nonce的值生成随机字符串，每次请求不能相同
        headers.put("X-Ca-Nonce", UUID.randomUUID().toString());
        Map<String, String> querys = new HashMap();
        Map<String, String> bodys = new HashMap();
        HashMap<Object, Object> paramMap = new HashMap<>();
        paramMap.put("order"," 明辉哥哥~你没有被黑哦~可惜不能完全自定义内容 ");
        paramMap.put("number","18737283528");
        bodys.put("paramMap", JSONObject.toJSONString(paramMap));
        bodys.put("phoneNumber", "13938682396");
        bodys.put("reqNo", "12844565481");
        bodys.put("smsSignId", "0000");
        bodys.put("smsTemplateNo", "0004");
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
//            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
