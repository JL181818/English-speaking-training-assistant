package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.response.CommonResponse;

import java.util.ArrayList;
import java.util.List;

public interface UserService {

    User AI_USER = new User(1, "AI", 1000, false, new ArrayList<>());

    User TEST_USER = new User(9, "Test", 1002, false, new ArrayList<>());

    CommonResponse handlePhoneLogin(long phone);

    CommonResponse handleCodeLogin(long phone, String code);

    CommonResponse handleGetInfo(String token);

}
