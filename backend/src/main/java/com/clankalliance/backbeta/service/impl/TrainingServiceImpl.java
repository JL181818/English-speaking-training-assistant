package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.repository.DialogRepository;
import com.clankalliance.backbeta.repository.TrainingDataRepository;
import com.clankalliance.backbeta.service.TrainingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TrainingServiceImpl implements TrainingService {

    @Resource
    private TrainingDataRepository trainingDataRepository;

    @Resource
    private DialogRepository dialogRepository;

}
