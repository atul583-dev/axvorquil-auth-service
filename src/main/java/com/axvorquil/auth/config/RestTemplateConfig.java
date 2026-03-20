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
        factory.setConnectTimeout(5000);   // 5 seconds — Azure cold start can be slow
        factory.setReadTimeout(10000);     // 10 seconds — Azure App Service cold starts take 6-10s
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}
