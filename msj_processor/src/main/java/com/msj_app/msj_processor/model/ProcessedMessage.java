package com.msj_app.msj_processor.model;


import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.msj_app.msj_processor.enums.MessageType;

import java.time.LocalDateTime;

@Document(collection = "processed_messages")
@Data
public class ProcessedMessage {
    
    @Id
    private String id;
    
    private String origin;
    private String destination;
    private MessageType messageType;
    private String content;
    
    private Long processingTime; // en milisegundos
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    private String error; // null si no hay error
}