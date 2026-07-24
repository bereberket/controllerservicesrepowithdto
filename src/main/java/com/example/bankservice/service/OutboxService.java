package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class OutboxService {
    private static final String ACCOUNT_CREATED =
            "ACCOUNT_CREATED";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void saveAccountCreatedEvent(
            AccountCreatedEvent event
    ){
        try{
            String payload =
                    objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent =
                    new OutboxEvent(
                            event.eventId().toString(),
                            ACCOUNT_CREATED,
                            event.accountNumber(),
                            payload
                            );
            outboxRepository.save(outboxEvent);
        }catch (JacksonException exception){
            throw new IllegalStateException(
                    "AccountCreatedEvent could not be serialized.",
                    exception
            );
        }
    }

    @Transactional
    public void requeueFailedEvent(String eventId){
        OutboxEvent outboxEvent = outboxRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Outbox event not found" + eventId
                        )
                );
        outboxEvent.prepareForRetry();
        //outboxRepository.save(outboxEvent); --->>> dirty checking
    }


}
