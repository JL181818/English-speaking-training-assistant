package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.request.training.GetDetailRequest;
import com.clankalliance.backbeta.request.training.GetListRequest;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.TrainingService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@CrossOrigin
@RestController
@RequestMapping("/api/trainingdata")
public class TrainingController {

    @Resource
    private TrainingService trainingService;

    @PostMapping("/getlist")
    public CommonResponse getList(@RequestBody GetListRequest request){
        return trainingService.handleGetList(request.getToken(), request.getPagenum(), request.getPagesize());
    }

    @PostMapping("/getdetail")
    public CommonResponse getList(@RequestBody GetDetailRequest request){
        return trainingService.handleGetDetail(request.getToken(), request.getId());
    }



}
