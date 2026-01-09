package com.msj_app.msj_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing  
public class MessageProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MessageProcessorApplication.class, args);
    }
}