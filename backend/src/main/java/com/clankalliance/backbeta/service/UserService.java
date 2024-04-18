package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.response.CommonResponse;

import java.util.List;

public interface UserService {

    CommonResponse handlePhoneLogin(long phone);

    CommonResponse handleCodeLogin(long phone, String code);

    CommonResponse handleGetInfo(String token);

}
