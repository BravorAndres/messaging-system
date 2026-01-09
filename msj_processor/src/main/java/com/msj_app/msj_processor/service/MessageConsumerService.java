package com.msj_app.msj_processor.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.msj_app.msj_processor.dto.MessageRequest;
import com.msj_app.msj_processor.dto.ProcessedMessage;
import com.msj_app.msj_processor.repository.ProcessedMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageConsumerService {
    
    private final ProcessedMessageRepository repository;
    
    @RabbitListener(queues = "messages.queue")
    public void processMessage(MessageRequest request, Message message) {
        try {
            log.info("Processing message: {} -> {}", request.getOrigin(), request.getDestination());
            
            // Obtener timestamp de recepción desde headers
            Long receivedAt = (Long) message.getMessageProperties().getHeader("receivedAt");
            if (receivedAt == null) {
                receivedAt = System.currentTimeMillis();
                log.warn("No receivedAt header found, using current time");
            }
            
            // Crear objeto para persistir
            ProcessedMessage processed = new ProcessedMessage();
            processed.setOrigin(request.getOrigin());
            processed.setDestination(request.getDestination());
            processed.setMessageType(request.getMessageType());
            processed.setContent(request.getContent());
            processed.setCreatedDate(LocalDateTime.now());
            
            // Validar límite de 3 mensajes en 24 horas
            LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
            List<ProcessedMessage> recentMessages = repository
                .findSuccessfulMessagesByDestinationSince(
                    request.getDestination(), 
                    last24Hours
                );
            
            if (recentMessages.size() >= 3) {
                // Excede el límite - registrar error
                String errorMsg = String.format(
                    "Destination %s has reached maximum messages (3) in 24h. Current count: %d",
                    request.getDestination(),
                    recentMessages.size()
                );
                processed.setError(errorMsg);
                log.warn("Message rejected: {}", errorMsg);
            } else {
                // Mensaje válido - sin error
                processed.setError(null);
                log.info("Message accepted: {} ({}/3 messages in 24h)", 
                    request.getDestination(), 
                    recentMessages.size() + 1
                );
            }
            
            // Calcular tiempo de procesamiento
            long processingTime = System.currentTimeMillis() - receivedAt;
            processed.setProcessingTime(processingTime);
            
            // Guardar en MongoDB
            ProcessedMessage saved = repository.save(processed);
            
            log.info("Message persisted with ID: {} - Processing time: {}ms", 
                saved.getId(), 
                processingTime
            );
            
        } catch (Exception e) {
            log.error("Error processing message from {} to {}: {}", 
                request.getOrigin(), 
                request.getDestination(), 
                e.getMessage(), 
                e
            );
        }
    }
}