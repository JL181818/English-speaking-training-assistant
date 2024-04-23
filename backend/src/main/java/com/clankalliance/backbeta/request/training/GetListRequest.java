package com.clankalliance.backbeta.request.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetListRequest {

    private String token;

    private int pagenum;

    private int pagesize;

}
