package com.teen.yygh.oss.serivec.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.teen.yygh.oss.serivec.FileService;
import com.teen.yygh.oss.utils.ConstantOssPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


/**
 * @author teen
 * @create 2022/4/1 9:16
 */
@Service
public class FileServiceImpl implements FileService {



    @Override
    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ConstantOssPropertiesUtil.ENDPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtil.SECRET;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantOssPropertiesUtil.BUCKET;
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = file.getOriginalFilename();
        //生成uuid保证文件名唯一  将uuid的 - 去掉
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        objectName = uuid + objectName;

        //按日期生成文件夹
        String timeUrl = new DateTime().toString("yyyy/MM/dd");
        objectName = timeUrl+"/"+objectName;


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            //从文件中获取流
            InputStream inputStream = file.getInputStream();
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, inputStream);

            String url = "https://" + bucketName+"."+endpoint+"/"+objectName;
            return url;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        }  catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
