package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.messaging.AccountCreatedPublisher;
import com.example.bankservice.messaging.RabbitPublishResult;
import com.example.bankservice.messaging.TransferCompletedEvent;
import com.example.bankservice.messaging.TransferCompletedPublisher;
import com.example.bankservice.repository.OutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxEventProcessor {
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final AccountCreatedPublisher accountCreatedPublisher;
    private final TransferCompletedPublisher transferCompletedPublisher;
    private final int maximumRetryCount;

    public OutboxEventProcessor(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            AccountCreatedPublisher accountCreatedPublisher,
            TransferCompletedPublisher transferCompletedPublisher,
            @Value("${outbox.worker.maximum-retry-count:3}")
            int maximumRetryCount

    ){
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.accountCreatedPublisher = accountCreatedPublisher;
        this.transferCompletedPublisher = transferCompletedPublisher;
        this.maximumRetryCount = maximumRetryCount;
    }

    @Transactional
    public void process(String eventId){
        OutboxEvent outboxEvent = outboxRepository
                .findByIdForUpdate(eventId)
                .orElse(null);
        if(outboxEvent == null){
            return;
        }
        if(outboxEvent.getStatus() != OutboxStatus.PENDING){
            return;
        }

        try{
            RabbitPublishResult result;

            if ("ACCOUNT_CREATED".equals(outboxEvent.getEventType())) {
                AccountCreatedEvent accountCreatedEvent =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                AccountCreatedEvent.class
                        );
                result = accountCreatedPublisher.publish(accountCreatedEvent);
            } else if ("TRANSFER_COMPLETED".equals(outboxEvent.getEventType())) {
                TransferCompletedEvent transferCompletedEvent =
                        objectMapper.readValue(
                                outboxEvent.getPayload(),
                                TransferCompletedEvent.class
                        );
                result = transferCompletedPublisher.publishResult(transferCompletedEvent);
            } else {
                outboxEvent.registerFailure(
                        "Unsupported outbox event type: " + outboxEvent.getEventType(),
                        maximumRetryCount
                );
                return;
            }

            if(result.successful()){
                outboxEvent.markPublished();
            }else{
                outboxEvent.registerFailure(
                        result.errorMessage(),
                        maximumRetryCount
                );
            }

        }catch (JacksonException exception){
            outboxEvent.registerFailure(
                    "Outbox payload could not be deserialized: "
                    +exception.getMessage(),
                    maximumRetryCount
            );
        }

    }
}
