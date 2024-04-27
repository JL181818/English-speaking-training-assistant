package com.clankalliance.backbeta.request.model;

import com.clankalliance.backbeta.entity.Dialog;
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

    public ModelMessage(Dialog d){
        content = d.getContent();
        role = d.getSender().getId() == UserService.AI_USER.getId()? "assistant": "user";
    }

}
