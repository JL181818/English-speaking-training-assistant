package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.response.CommonResponse;

public interface DictionaryService {

    CommonResponse saveWord(String word, String trans, String example, String exampleTrans);

    CommonResponse getWord(String token, String word);

}
