package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedPublisher {
    private final RabbitTemplate rabbitTemplate;

    public AccountCreatedPublisher(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String message){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BANK_EVENTS_EXCHANGE,
                RabbitMqConfig.ACCOUNT_CREATED_ROUTING_KEY,
                message
        );
    }

}
