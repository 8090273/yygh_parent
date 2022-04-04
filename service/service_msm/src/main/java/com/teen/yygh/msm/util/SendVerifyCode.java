package com.teen.yygh.msm.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 实现InitializingBean接口可用在初始化spring时注入配置文件参数
 * @author teen
 * @create 2022/3/29 23:38
 */
@Component
public class SendVerifyCode implements InitializingBean {

    @Value("${com.teen.yygh.code.host}")
    private String p_host;
    @Value("${com.teen.yygh.code.path}")
    private String p_path;
    @Value("${com.teen.yygh.code.method}")
    private String p_method;
    @Value("${com.teen.yygh.code.appcode}")
    private String p_appcode;

    public static String HOST;
    public static String PATH;
    public static String METHOD;
    public static String APPCODE;


    public static String sendSixCode(String phone){
        String host = HOST;
        String path = PATH;
        String method = METHOD;
        String appcode = APPCODE;
        Map<String, String> headers = new HashMap<>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //需要给X-Ca-Nonce的值生成随机字符串，每次请求不能相同
        headers.put("X-Ca-Nonce", UUID.randomUUID().toString());
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        bodys.put("phoneNumber", phone);
        bodys.put("reqNo", "201905050223");
        bodys.put("smsSignId", "0000");
        bodys.put("smsTemplateNo", "0003");
//        bodys.put("verifyCode", "发中文");


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
            String result = EntityUtils.toString(response.getEntity());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 实现InitializingBean接口是直接调用afterPropertiesSet方法，
     * 比通过反射调用init-method指定的方法效率要高一点，
     * 但是init-method方式消除了对spring的依赖。
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        HOST = this.p_host;
        PATH = this.p_path;
        METHOD = this.p_method;
        APPCODE = this.p_appcode;
    }
}
