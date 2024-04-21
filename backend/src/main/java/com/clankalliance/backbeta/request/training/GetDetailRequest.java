package com.clankalliance.backbeta.request.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetDetailRequest {

    private String token;
    private String id;

}
