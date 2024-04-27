package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.entity.Dialog;

import java.util.List;

public interface AIService {

    String invokeModel(List<Dialog> dialogs);

}
