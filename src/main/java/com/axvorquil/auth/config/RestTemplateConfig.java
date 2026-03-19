package com.axvorquil.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);   // 4 seconds
        factory.setReadTimeout(4000);      // 4 seconds
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
