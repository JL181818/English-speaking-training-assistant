package com.clankalliance.backbeta.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
@Configuration
public class BloomFilterProperties {
    /**
     * 预期插入量
     */
    private Long expectedInsertions = 60000L;
    /**
     * 误判率（大于0，小于1.0）
     */
    private Double fpp = 0.01D;

    public Long getExpectedInsertions() {
        return expectedInsertions;
    }

    public void setExpectedInsertions(Long expectedInsertions) {
        this.expectedInsertions = expectedInsertions;
    }

    public Double getFpp() {
        return fpp;
    }

    public void setFpp(Double fpp) {
        this.fpp = fpp;
    }

}