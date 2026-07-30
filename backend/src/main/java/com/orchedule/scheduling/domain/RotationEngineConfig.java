package com.orchedule.scheduling.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RotationEngineConfig {

    @Bean
    public RotationEngine rotationEngine() {
        return new RotationEngine();
    }
}
