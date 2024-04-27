package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.request.user.*;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;



@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserService userService;



    @PostMapping("/tokencheck")
    public CommonResponse tokenCheck(@RequestBody TokenCheckRequest request){
        return tokenUtil.tokenCheck(request.getToken());
    }

    @PostMapping("/phonecode")
    public CommonResponse handlePhoneLogin(@RequestBody PhoneCheckRequest request){
        return userService.handlePhoneLogin(request.getPhone());
    }

    @PostMapping("/phonelogin")
    public CommonResponse handleCodeLogin(@RequestBody PhoneCheckRequest request){
        return userService.handleCodeLogin(request.getPhone(), request.getCode());
    }

    @PostMapping("/myinfo")
    public CommonResponse handleGetInfo(@RequestBody TokenCheckRequest request){
        return userService.handleGetInfo(request.getToken());
    }

}
