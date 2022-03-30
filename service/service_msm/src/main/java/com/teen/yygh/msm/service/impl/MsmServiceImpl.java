package com.teen.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.msm.service.MsmService;
import com.teen.yygh.msm.util.SendVerifyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author teen
 * @create 2022/3/29 23:17
 */
@Service
public class MsmServiceImpl implements MsmService {


    /**
     * 获取验证码
     * @param phone
     * @return
     */
    @Override
    public String getCode(String phone) {
        String code = SendVerifyCode.sendSixCode(phone);
        //如果返回了空串“”，说明sendSixCode异常
        if (code.equals("")){
            return "";
        }
        HashMap<String,String> codeMap = JSONObject.parseObject(code, HashMap.class);
        String message = codeMap.get("message");
        if (message.equals("SUCCESS")){
            String verifyCode = codeMap.get("verificationCode");
            System.out.println(verifyCode);
            return verifyCode;
        }else {
            System.out.println(codeMap);
            //发送失败则返回空
            return "";
        }

    }
}
