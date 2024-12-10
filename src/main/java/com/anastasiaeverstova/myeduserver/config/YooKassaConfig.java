package com.anastasiaeverstova.myeduserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.anastasiaeverstova.myeduserver.yookassa.Yookassa;

@Configuration
public class YooKassaConfig {

    @Bean
    public Yookassa yookassa() {
        int shopIdentifier = 388086;
        String shopToken = "test_qqG-rcjApZwhlvPRNiN-ADPZZ1b-WhlTzmW1KmJO9wU";
        return Yookassa.initialize(shopIdentifier, shopToken);
    }
}
