package com.msj_app.api_gateway.dto;

import com.msj_app.api_gateway.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank
    private String origin;
    
    @NotBlank
    private String destination;
    
    @NotNull
    private MessageType messageType;
    
    @NotBlank
    private String content;
}
