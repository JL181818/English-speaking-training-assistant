package com.clankalliance.backbeta.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordRequest {

    private String token;

    private String word;

    private String trans;

    private String example;

    private String exampleTrans;

}
