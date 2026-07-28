package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.messaging.AccountCreatedPublisher;
import com.example.bankservice.messaging.RabbitPublishResult;
import com.example.bankservice.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventProcessorTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccountCreatedPublisher accountCreatedPublisher;

    @InjectMocks
    private OutboxEventProcessor outboxEventProcessor;

    @BeforeEach
    void setUp() {
        outboxEventProcessor =
                new OutboxEventProcessor(
                        outboxRepository,
                        objectMapper,
                        accountCreatedPublisher,
                        3
                );
    }

    @Test
    void process_shouldDoNothing_whenEventDoesNotExist() {
        // ARRANGE
        String eventId =
                "88888888-8888-8888-8888-888888888888";

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.empty());

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        verify(outboxRepository).findById(eventId);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(accountCreatedPublisher);
    }

    @Test
    void process_shouldDoNothing_whenEventIsAlreadyPublished() {
        // ARRANGE
        String eventId =
                "77777777-7777-7777-7777-777777777777";

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        eventId,
                        "ACCOUNT_CREATED",
                        "TR26568",
                        "{\"accountNumber\":\"TR26568\"}"
                );

        outboxEvent.markPublished();

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.of(outboxEvent));

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        assertEquals(
                OutboxStatus.PUBLISHED,
                outboxEvent.getStatus()
        );
        assertNotNull(outboxEvent.getPublishedAt());

        verify(outboxRepository).findById(eventId);
        verifyNoInteractions(objectMapper);
        verifyNoInteractions(accountCreatedPublisher);
    }

    @Test
    void process_shouldMarkEventPublished_whenPublishSucceeds()
            throws JacksonException {
        // ARRANGE
        UUID eventUuid =
                UUID.fromString(
                        "33333333-3333-3333-3333-333333333333"
                );

        String eventId = eventUuid.toString();

        String payload =
                "{\"accountNumber\":\"TR23567\"}";

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        eventId,
                        "ACCOUNT_CREATED",
                        "TR23567",
                        payload
                );

        AccountCreatedEvent accountCreatedEvent =
                new AccountCreatedEvent(
                        eventUuid,
                        "TR23567",
                        "berk",
                        "Ana hesap",
                        Instant.parse("2026-07-27T10:00:00Z")
                );

        RabbitPublishResult publishResult =
                new RabbitPublishResult(
                        true,
                        null
                );

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.of(outboxEvent));

        when(objectMapper.readValue(
                payload,
                AccountCreatedEvent.class
        )).thenReturn(accountCreatedEvent);

        when(accountCreatedPublisher.publish(accountCreatedEvent))
                .thenReturn(publishResult);

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        assertEquals(
                OutboxStatus.PUBLISHED,
                outboxEvent.getStatus()
        );
        assertNotNull(outboxEvent.getPublishedAt());
        assertEquals(0, outboxEvent.getRetryCount());
        assertNull(outboxEvent.getLastError());

        verify(outboxRepository).findById(eventId);

        verify(objectMapper).readValue(
                payload,
                AccountCreatedEvent.class
        );

        verify(accountCreatedPublisher)
                .publish(accountCreatedEvent);
    }

    @Test
    void process_shouldRegisterFailure_whenPublishFails()
            throws JacksonException {
        // ARRANGE
        UUID eventUuid =
                UUID.fromString(
                        "11111111-1111-1111-1111-111111111111"
                );

        String eventId = eventUuid.toString();

        String payload =
                "{\"accountNumber\":\"TR23568\"}";

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        eventId,
                        "ACCOUNT_CREATED",
                        "TR23568",
                        payload
                );

        AccountCreatedEvent accountCreatedEvent =
                new AccountCreatedEvent(
                        eventUuid,
                        "TR23568",
                        "berko",
                        "Ana hesap",
                        Instant.parse("2025-07-27T10:00:00Z")
                );

        RabbitPublishResult publishResult =
                new RabbitPublishResult(
                        false,
                        "Exceed try"
                );

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.of(outboxEvent));

        when(objectMapper.readValue(
                payload,
                AccountCreatedEvent.class
        )).thenReturn(accountCreatedEvent);

        when(accountCreatedPublisher.publish(accountCreatedEvent))
                .thenReturn(publishResult);

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        assertEquals(
                OutboxStatus.PENDING,
                outboxEvent.getStatus()
        );
        assertEquals(1, outboxEvent.getRetryCount());
        assertEquals(
                "Exceed try",
                outboxEvent.getLastError()
        );
        assertNull(outboxEvent.getPublishedAt());

        verify(outboxRepository).findById(eventId);

        verify(objectMapper).readValue(
                payload,
                AccountCreatedEvent.class
        );

        verify(accountCreatedPublisher)
                .publish(accountCreatedEvent);
    }

    @Test
    void process_shouldRegisterFailure_whenPayloadCannotBeDeserialized() throws JacksonException{
        UUID eventUuid =
                UUID.fromString(
                        "12121212-1212-1212-1212-121212121212"
                );

        String eventId = eventUuid.toString();

        String payload =
                "{accountNumber:}";

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        eventId,
                        "ACCOUNT_CREATED",
                        "",
                        payload
                );

        when(outboxRepository.findById(eventId)).
                thenReturn(Optional.of(outboxEvent));


        JacksonException deserializationException = mock(JacksonException.class);

        when(deserializationException.getMessage())
                .thenReturn("INVALID JSON");

        when(objectMapper.readValue(payload, AccountCreatedEvent.class))
                .thenThrow(deserializationException);

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        assertEquals(
                OutboxStatus.PENDING,
                outboxEvent.getStatus()
        );

        assertEquals(1, outboxEvent.getRetryCount());

        assertEquals(
                "Outbox payload could not be deserialized: INVALID JSON",
                outboxEvent.getLastError()
        );

        assertNull(outboxEvent.getPublishedAt());

        verify(outboxRepository).findById(eventId);

        verify(objectMapper).readValue(
                payload,
                AccountCreatedEvent.class
        );

        verifyNoInteractions(accountCreatedPublisher);

    }

    @Test
    void process_shouldMarkEventFailed_whenMaximumRetryCountIsReached() throws JacksonException{
        UUID eventUuid =
                UUID.fromString(
                        "23232323-2323-2323-2323-232323232323"
                );

        String eventId = eventUuid.toString();


        String payload =
                "{\"accountNumber\":\"TR23565\"}";

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        eventId,
                        "ACCOUNT_CREATED",
                        "TR23565",
                        payload
                );
        outboxEvent.registerFailure(
                "First publish attempt failed",
                3
        );

        outboxEvent.registerFailure(
                "Second publish attempt failed",
                3
        );

        AccountCreatedEvent accountCreatedEvent =
                new AccountCreatedEvent(
                        eventUuid,
                        "TR23565",
                        "berko",
                        "Ana hesap",
                        Instant.parse("2026-02-27T10:00:00Z")
                );


        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue(
                payload,
                AccountCreatedEvent.class
        )).thenReturn(accountCreatedEvent);
        RabbitPublishResult publishResult =
                new RabbitPublishResult(
                        false,
                        "Maximum retry count is reached"
                );
        assertEquals(OutboxStatus.PENDING,outboxEvent.getStatus());
        assertEquals(2,outboxEvent.getRetryCount());

        when(accountCreatedPublisher.publish(accountCreatedEvent))
                .thenReturn(publishResult);

        //act
        outboxEventProcessor.process(eventId);

        //assertion
        assertEquals(3,outboxEvent.getRetryCount());
        assertEquals(OutboxStatus.FAILED,outboxEvent.getStatus());
        assertEquals("Maximum retry count is reached",outboxEvent.getLastError());
        assertNull(outboxEvent.getPublishedAt());

        verify(outboxRepository).findById(eventId);
        verify(objectMapper).readValue(
                payload,
                AccountCreatedEvent.class
        );

        verify(accountCreatedPublisher)
                .publish(accountCreatedEvent);
    }

}