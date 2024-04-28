package com.clankalliance.backbeta.request.model;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.TrainingData;
import com.clankalliance.backbeta.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvokeModelRequest {

    private String model;
    private List<ModelMessage> messages;
    private Double temperature;
    private Integer top_p;
    private Integer n;
    private Integer max_token;
    private String stop;
    private Boolean stream;
    private Integer presence_penalty;
    private Integer frequency_penalty;
    private String user;
    private Integer repetition_penalty;
    private Integer session_id;
    private Boolean ignore_eos;
    private Boolean skip_special_tokens;
    private Integer top_k;

    public InvokeModelRequest(User user, List<Dialog> dialogs){
        model = "internlm2";
        messages = new ArrayList<>();
        for(Dialog d: dialogs){
            messages.add(new ModelMessage(d));
        }
        temperature = 0.7;
        top_p = 1;
        n = 1;
        max_token = null;
        stop = null;
        stream = false;
        presence_penalty = 0;
        frequency_penalty = 0;
        this.user = user.getNickName();
        repetition_penalty = 1;
        session_id = -1;
        ignore_eos = false;
        skip_special_tokens = true;
        top_k = 40;
    }

}
