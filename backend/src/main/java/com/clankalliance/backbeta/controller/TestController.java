package com.clankalliance.backbeta.controller;

import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.repository.UserRepository;
import com.clankalliance.backbeta.utils.RedisUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class TestController {
    private StringRedisTemplate redisTemplate;

    @Resource
    private UserRepository userRepository;

    @GetMapping("/redisTest")
    public String addToRedis() {
//        RedisUtils.add("1", "1", redisTemplate);
        User u = userRepository.getById(9L);
        List<TrainingData> trainingData = u.getTrainingDataList();
        String test = "";
        for(TrainingData t: trainingData){
            test += t.getId() + " ";
        }
        return test;
    }
}
