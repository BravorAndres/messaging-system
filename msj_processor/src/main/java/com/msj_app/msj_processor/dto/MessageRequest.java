package com.msj_app.msj_processor.dto;


import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;

import com.msj_app.msj_processor.enums.MessageType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest implements Serializable {
    private String origin;
    private String destination;
    private MessageType messageType;
    private String content;
}