package com.msj_app.msj_processor.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msj_app.msj_processor.dto.ProcessedMessage;
import com.msj_app.msj_processor.repository.ProcessedMessageRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class QueryController {
    
    private final ProcessedMessageRepository repository;
    
    @GetMapping("/destination/{destination}")
    public ResponseEntity<Map<String, Object>> getMessagesByDestination(
            @PathVariable String destination) {
        
        List<ProcessedMessage> messages = repository.findByDestination(destination);
        
        return ResponseEntity.ok(Map.of(
            "destination", destination,
            "totalMessages", messages.size(),
            "messages", messages
        ));
    }
    
    @GetMapping
    public ResponseEntity<List<ProcessedMessage>> getAllMessages() {
        List<ProcessedMessage> messages = repository.findAll();
        return ResponseEntity.ok(messages);
    }
}