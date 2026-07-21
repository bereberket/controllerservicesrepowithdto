package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(AccountCreatedListener.class);
    @RabbitListener(queues = RabbitMqConfig.ACCOUNT_CREATED_QUEUE)
    public void handle(AccountCreatedEvent createdEvent){
        log.info("Account created event received. Account Number:  {}",
                createdEvent.accountNumber()
        );



        log.info(
                "Account created event processed successfully. Account Number: {}",
                createdEvent.accountNumber()
        );

    }
}
