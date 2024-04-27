package com.clankalliance.backbeta.service.impl;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.service.AIService;
import com.clankalliance.backbeta.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIServiceImplTest {

    @InjectMocks
    private AIServiceImpl service;

    @BeforeEach
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void invokeModel() {
        System.out.println(service.invokeModel(
                new TrainingData(
                        "",
                        new Date(),
                        UserService.TEST_USER,
                        1,
                        List.of(new Dialog[]{
                                new Dialog("1", new Date(), "Hello", UserService.TEST_USER, null)
                        }))));
    }
}
