package com.msj_app.msj_processor.dto;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
public class MessageDocument {

    @Id
    private String id;

    private String origin;
    private String destination;
    private String messageType;
    private String content;

    private Long processingTime;
    private Instant createdDate;
    private String error;
}
