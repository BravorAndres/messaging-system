package com.msj_app.api_gateway.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msj_app.api_gateway.dto.MessageRequest;
import com.msj_app.api_gateway.repository.AuthorizedLineRepository;
import com.msj_app.api_gateway.service.MessagePublisherService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    
    private final AuthorizedLineRepository lineRepository;
    private final MessagePublisherService publisher;
    
    @PostMapping
    public ResponseEntity<?> sendMessage(@Valid @RequestBody MessageRequest request) {
       // System.out.println("    >> new req  <<");
        // Validar línea de origen
        if (!lineRepository.existsByLineNumberAndActiveTrue(request.getOrigin())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Unauthorized origin line"));
        }
        
        // Publicar a RabbitMQ
        publisher.publishMessage(request);
        
        return ResponseEntity.accepted()
            .body(Map.of("status", "Message queued for processing"));
    }
}
