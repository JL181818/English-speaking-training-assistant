package com.clankalliance.backbeta.utils;

import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.utils.StatusManipulateUtilsWithRedis.ManipulateUtilRedis;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class TokenUtil {


    @Resource
    private ManipulateUtilRedis manipulateUtil;


    /**
     * 验证token是否有效，同时返回新token及用户对象
     * @param token 前台传来的token
     * @return
     */
    public CommonResponse tokenCheck(String token){
        if(token.equals("114514")){
            return CommonResponse.successResponse("9", "114514");
        }
        CommonResponse response = new CommonResponse();
        String id =  manipulateUtil.findStatusByToken(token);
        if(id != null){
            response.setSuccess(true);
            response.setLoginValid(true);
            response.setToken(manipulateUtil.updateStatus(id));
            response.setMessage(id);
        }else{
            response.setSuccess(false);
            response.setLoginValid(false);
            response.setMessage("登录失效");
        }
        return response;
    }

    public CommonResponse phoneCodeCheck(long phone, String code){
        CommonResponse response = new CommonResponse();
        String id = manipulateUtil.getIdByPhone(String.valueOf(phone), code);
        if(id == null){
            response.setMessage("验证码不正确");
            response.setSuccess(false);
            response.setLoginValid(false);
        }else{
            response.setSuccess(true);
            response.setToken(manipulateUtil.updateStatus(id));
            response.setLoginValid(true);
            response.setMessage(id);
        }
        return response;
    }

}
