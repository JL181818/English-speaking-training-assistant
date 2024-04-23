package com.clankalliance.backbeta.service;

import com.clankalliance.backbeta.response.CommonResponse;

public interface TrainingService {

    CommonResponse handleGetList(String token, int pageNum, int pageSize);

    CommonResponse handleGetDetail(String token, String id);

}
