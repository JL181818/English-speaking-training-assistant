package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.service.AIService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIServiceImpl implements AIService {

    @Override
    public String invokeModel(List<Dialog> dialogs){
        //TODO: 转换数据结构，调用接口发送给大模型处理
        return "未实现";
    }

}
