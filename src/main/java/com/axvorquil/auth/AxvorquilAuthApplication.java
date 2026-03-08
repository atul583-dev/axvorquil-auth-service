package com.axvorquil.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AxvorquilAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AxvorquilAuthApplication.class, args);
    }
}
