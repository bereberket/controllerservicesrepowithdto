package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import com.example.bankservice.entity.ProcessedMessage;
import com.example.bankservice.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class AccountCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(AccountCreatedListener.class);

    private final ProcessedMessageRepository processedMessageRepository;

    public AccountCreatedListener(ProcessedMessageRepository processedMessageRepository){
        this.processedMessageRepository = processedMessageRepository;
    }





    @Transactional
    @RabbitListener(queues = RabbitMqConfig.ACCOUNT_CREATED_QUEUE)
    public void handle(AccountCreatedEvent createdEvent){
        String eventId = createdEvent.eventId().toString();
        if(processedMessageRepository.existsById(eventId)){
            log.warn("This event already exist. Event ID: {}", eventId);
        return;
        }
        log.info("Account created event received. Account Number:  {}",
                createdEvent.accountNumber()
        );

        ProcessedMessage processedMessage =
                new ProcessedMessage(
                        eventId,
                        "ACCOUNT_CREATED",
                        Instant.now()
                );

        processedMessageRepository.save(processedMessage);



        log.info(
                "Account created event processed successfully. Account Number: {}",
                createdEvent.accountNumber()
        );

    }
}
