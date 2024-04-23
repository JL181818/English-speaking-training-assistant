package com.clankalliance.backbeta.utils;

import com.clankalliance.backbeta.response.CommonResponse;

public class ErrorHandle {

    public static CommonResponse handleSaveException(Exception e, CommonResponse response){
        response.setSuccess(false);
        response.setMessage("保存失败");
        response.setContent(e);
        return response;
    }

    public static CommonResponse handleSaveException(String message, CommonResponse response){
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    public static CommonResponse handleDeleteException(Exception e, CommonResponse response){
        response.setSuccess(false);
        response.setMessage("删除失败");
        response.setContent(e);
        return response;
    }

    public static CommonResponse handleNotExist(String className, CommonResponse response){
        response.setSuccess(false);
        response.setMessage(className + "不存在");
        return response;
    }
}
