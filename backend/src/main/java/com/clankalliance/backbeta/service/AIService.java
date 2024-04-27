package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.TrainingData;

import java.util.List;

public interface AIService {

    String invokeModel(TrainingData trainingData);

}
