package com.msj_app.msj_processor.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.msj_app.msj_processor.dto.ProcessedMessage;

@Repository
public interface ProcessedMessageRepository extends MongoRepository<ProcessedMessage, String> {
    
    // Encuentra mensajes exitosos (sin error) enviados a un destino desde una fecha específica
    @Query("{ 'destination': ?0, 'createdDate': { $gte: ?1 }, 'error': null }")
    List<ProcessedMessage> findSuccessfulMessagesByDestinationSince(
        String destination, 
        LocalDateTime since
    );
    
    // Encuentra todos los mensajes (con o sin error) enviados a un destino
    List<ProcessedMessage> findByDestination(String destination);
}