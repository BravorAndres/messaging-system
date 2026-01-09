package com.msj_app.msj_processor.dto;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import com.msj_app.msj_processor.enums.MessageType;

import lombok.Data;

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
    
    private String error;
}
