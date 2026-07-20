package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedListener {
    @RabbitListener(queues = RabbitMqConfig.ACCOUNT_CREATED_QUEUE)
    public void handle(AccountCreatedEvent createdEvent){
        System.out.println("Rabbit message " + createdEvent);
    }
}
