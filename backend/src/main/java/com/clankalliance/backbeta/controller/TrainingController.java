package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.request.user.UserLoginRequest;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.TrainingService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@CrossOrigin
@RestController
@RequestMapping("/api/trainingdata")
public class TrainingController {

    @Resource
    private TrainingService trainingService;





}
