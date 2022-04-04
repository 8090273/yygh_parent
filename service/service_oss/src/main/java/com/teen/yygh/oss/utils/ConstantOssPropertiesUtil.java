package com.teen.yygh.oss.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author teen
 * @create 2022/4/1 9:40
 */
@Component
public class ConstantOssPropertiesUtil implements InitializingBean {
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.secret}")
    private String secret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    public static String ENDPOINT;
    public static String ACCESS_KEY_ID;
    public static String SECRET;
    public static String BUCKET;

    @Override
    public void afterPropertiesSet() throws Exception {
        ENDPOINT = endpoint;
        ACCESS_KEY_ID = accessKeyId;
        SECRET = secret;
        BUCKET = bucket;
    }
}
