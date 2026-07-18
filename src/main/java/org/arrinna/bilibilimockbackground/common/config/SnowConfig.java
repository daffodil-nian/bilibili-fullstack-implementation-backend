package org.arrinna.bilibilimockbackground.common.config;

import org.arrinna.bilibilimockbackground.common.algorithm.SnowAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowConfig {

    @Value("${snow.worker-id:1}")
    private long workerId;

    @Bean
    public SnowAlgorithm snowAlgorithm(){
        return new SnowAlgorithm(workerId);
    }
}
