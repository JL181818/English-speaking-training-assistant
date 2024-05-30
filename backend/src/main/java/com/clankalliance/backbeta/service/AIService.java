package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.User;
import com.clankalliance.backbeta.redisDataBody.DialogDataBody;

import java.util.List;

public interface AIService {

    String invokeModel(String chat);

//    String invokeModel(User user, List<DialogDataBody> dialogs);

}
