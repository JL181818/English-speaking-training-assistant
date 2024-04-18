package com.clankalliance.backbeta.utils.StatusManipulateUtilsWithRedis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneStatusNode {
    private String userId;

    private String verifyCode;


    /**
     * 根据用户id自动生成状态
     * 由用户id与状态更新时间拼接加密获得token,作为一个随时间变化的唯一身份标识
     * @param userId
     */
    public PhoneStatusNode(String userId){
        this.userId = userId;
        //五位验证码
        int random = (int) ((99999-10000+1)*Math.random()+10000);
        this.verifyCode = "" + random;
    }
}
