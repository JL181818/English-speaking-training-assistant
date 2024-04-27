package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.request.user.*;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.AIService;
import com.clankalliance.backbeta.service.UserService;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private UserService userService;

    @Resource
    private AIService aiService;


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

    @PostMapping("/test")
    public CommonResponse test(@RequestBody TokenCheckRequest request){
        return CommonResponse.successResponse(
                aiService.invokeModel(
                    new TrainingData(
                            "",
                            new Date(),
                            UserService.TEST_USER,
                            1,
                            List.of(new Dialog[]{
                                    new Dialog("1", new Date(), "Hello", UserService.TEST_USER, null)
                                }
                            )
                    )
                )
        );
    }

}
