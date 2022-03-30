package test;

import com.teen.yygh.msm.util.HttpUtils;
import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author teen
 * @create 2022/3/29 17:47
 */
public class GetMsmTest {
    public static void main(String[] args) {
        String host = "https://miitangs09.market.alicloudapi.com";
        String path = "/v1/tools/sms/code/sender";
        String method = "POST";
        String appcode = "556af59039b148d1acf655937417a456";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //需要给X-Ca-Nonce的值生成随机字符串，每次请求不能相同
        headers.put("X-Ca-Nonce", UUID.randomUUID().toString());
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("phoneNumber", "18737283528");
        bodys.put("reqNo", "123456987");
        bodys.put("smsSignId", "0000");
        bodys.put("smsTemplateNo", "0003");
//        bodys.put("verifyCode", "verifyCode");


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
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
