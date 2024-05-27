package com.clankalliance.backbeta.request.model;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.redisDataBody.DialogDataBody;
import com.clankalliance.backbeta.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelMessage {

    private String content;
    private String role;

    public ModelMessage(DialogDataBody d){
        content = d.getContent();
        role = d.getSenderId() == UserService.AI_USER.getId()? "assistant": "user";
    }

}
