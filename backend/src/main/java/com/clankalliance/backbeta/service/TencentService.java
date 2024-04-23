package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.response.CommonResponse;

public interface TencentService {
    /**
     * 发送短信
     * @param phone 电话号码
     * @param code 验证码
     * @return
     */
    CommonResponse sendSms(String phone, String code);

    boolean getTencentCaptchaResult(String ticket, String randstr);
}
