package com.teen.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.teen.yygh.common.exception.YyghException;
import com.teen.yygh.common.result.ResultCodeEnum;
import com.teen.yygh.msm.service.MsmService;
import com.teen.yygh.msm.util.SendVerifyCode;
import com.teen.yygh.vo.msm.MsmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public String send(MsmVo msmVo) {
        // 如果手机号不为空
        if (!StringUtils.isEmpty(msmVo.getPhone())){
//            String code = (String)msmVo.getParam().get("code");
            String code = this.getCode(msmVo.getPhone());
            System.out.println("发送了短信！: "+ code);
            return code;
        }
        System.out.println("--------------短信发送失败！手机号为空-------------------");
        return "";
    }
}
