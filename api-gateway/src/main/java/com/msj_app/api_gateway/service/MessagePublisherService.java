package com.msj_app.api_gateway.service;

import java.util.Date;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties; 
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.msj_app.api_gateway.dto.MessageRequest;
import com.msj_app.api_gateway.rabbitmq.RabbitMQConfig;  

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessagePublisherService {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishMessage(MessageRequest request) {
        // Crear propiedades del mensaje
        MessageProperties props = new MessageProperties();
        props.setTimestamp(new Date());
        props.setHeader("receivedAt", System.currentTimeMillis());
        
        // Convertir objeto a mensaje AMQP
        Message message = rabbitTemplate.getMessageConverter()
            .toMessage(request, props);
        
        // Enviar a RabbitMQ
        
        rabbitTemplate.send(
            RabbitMQConfig.EXCHANGE_NAME, 
            RabbitMQConfig.ROUTING_KEY, 
            message
        );
    }
}