package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.repository.DialogRepository;
import com.clankalliance.backbeta.repository.TrainingDataRepository;
import com.clankalliance.backbeta.response.CommonResponse;
import com.clankalliance.backbeta.service.TrainingService;
import com.clankalliance.backbeta.utils.TokenUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    @Resource
    private TokenUtil tokenUtil;

    @Resource
    private TrainingDataRepository trainingDataRepository;


    public CommonResponse handleGetList(String token, int pageNum, int pageSize){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getLoginValid())
            return response;
        PageRequest pageRequest = PageRequest.of(pageNum, pageSize, Sort.Direction.DESC, "time");
        try{
            Page<TrainingData> trainingDataPage = trainingDataRepository.findByUserId(Long.parseLong(response.getMessage()), pageRequest);
            List<TrainingData> trainingData = trainingDataPage.stream().toList();
            for(TrainingData t: trainingData){
                List<Dialog> dialogs = t.getDialogs();
                Collections.sort(dialogs, (o1, o2) -> Long.compare(o2.getTime().getTime(), o1.getTime().getTime()));
                t.setDialogs(dialogs);

            }
            response.setContent(trainingData);
            response.setMessage("查找成功");
        }catch (Exception e){
            CommonResponse.errorResponse("查找失败", response, e);
        }
        return response;
    }

    public CommonResponse handleGetDetail(String token, String id){
        CommonResponse response = tokenUtil.tokenCheck(token);
        if(!response.getLoginValid())
            return response;
        Optional<TrainingData> top = trainingDataRepository.findById(id);
        if(top.isEmpty()){
            return CommonResponse.errorResponse("训练数据不存在", response);
        }else{
            response.setContent(top.get());
            return CommonResponse.successResponse("查找成功", response);
        }
    }

}
