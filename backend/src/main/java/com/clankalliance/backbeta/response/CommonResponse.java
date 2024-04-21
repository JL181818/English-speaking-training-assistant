package com.clankalliance.backbeta.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T1> {

    //业务成功与失败
    private Boolean success = true;
    //返回信息
    private String message;

    //返回泛型数据 自定义类型
    private T1 content;

    private String token;

    private Boolean loginValid;

    public static CommonResponse errorResponse(String errorMessage, String token){
        return new CommonResponse(false,errorMessage,null,token, true);
    }

    public static CommonResponse errorResponse(String errorMessage){
        return new CommonResponse(false,errorMessage,null,null, true);
    }

    public static CommonResponse errorResponse(String errorMessage, CommonResponse oldResponse, Exception e){
        oldResponse.setMessage(errorMessage);
        oldResponse.setSuccess(false);
        oldResponse.setContent(e);
        return oldResponse;
    }

    public static CommonResponse errorResponse(String errorMessage, CommonResponse oldResponse){
        oldResponse.setMessage(errorMessage);
        oldResponse.setSuccess(false);
        return oldResponse;
    }

    public static CommonResponse successResponse(String successMessage, String token){
        return new CommonResponse(true,successMessage,null,token,true);
    }

    public static CommonResponse successResponse(String errorMessage){
        return new CommonResponse(true ,errorMessage,null,null, true);
    }

    public static CommonResponse successResponse(String successMessage, CommonResponse oldResponse){
        oldResponse.setMessage(successMessage);
        oldResponse.setSuccess(true);
        return oldResponse;
    }

}
