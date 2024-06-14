package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.request.WordRequest;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.DictionaryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@CrossOrigin
@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    @Resource
    private DictionaryService dictionaryService;

    @PostMapping("/getword")
    public CommonResponse getWord(@RequestBody WordRequest request){
        return dictionaryService.getWord(request.getToken(), request.getWord());
    }

    @PostMapping("/saveword")
    public CommonResponse saveWord(@RequestBody WordRequest request){
        return dictionaryService.saveWord(request.getWord(), request.getTrans(), request.getExample(), request.getExampleTrans());
    }

}
