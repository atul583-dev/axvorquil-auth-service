package com.axvorquil.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing   // enables @CreatedDate and @LastModifiedDate on User model
public class MongoConfig {
}
