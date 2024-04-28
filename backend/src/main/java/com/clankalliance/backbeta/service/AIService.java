package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.User;

import java.util.List;

public interface AIService {

    String invokeModel(User user, List<Dialog> dialogs);

}
