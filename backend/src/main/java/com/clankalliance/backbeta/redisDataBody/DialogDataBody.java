package com.clankalliance.backbeta.redisDataBody;

import com.clankalliance.backbeta.entity.Dialog;
import com.clankalliance.backbeta.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogDataBody {

    private String id;

    private Date time;

    private Long senderId;

    private String content;

    private String correction;

    private Double score;

    public Dialog toDialog(User sender){
        return new Dialog(id, time, content, sender, correction, score);
    }

}
