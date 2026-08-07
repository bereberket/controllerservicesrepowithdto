package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.messaging.AccountCreatedPublisher;
import com.example.bankservice.messaging.RabbitPublishResult;
import com.example.bankservice.messaging.TransferCompletedEvent;
import com.example.bankservice.messaging.TransferCompletedPublisher;
import com.example.bankservice.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
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

    @Mock
    private TransferCompletedPublisher transferCompletedPublisher;

    private OutboxEventProcessor outboxEventProcessor;

    @BeforeEach
    void setUp() {
        outboxEventProcessor =
                new OutboxEventProcessor(
                        outboxRepository,
                        objectMapper,
                        accountCreatedPublisher,
                        transferCompletedPublisher,
                        3
                );
    }

    @Test
    void process_shouldDoNothing_whenEventDoesNotExist() {
        // ARRANGE
        String eventId =
                "88888888-8888-8888-8888-888888888888";

        when(outboxRepository.findByIdForUpdate(eventId))
                .thenReturn(Optional.empty());

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        verify(outboxRepository).findByIdForUpdate(eventId);
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

        when(outboxRepository.findByIdForUpdate(eventId))
                .thenReturn(Optional.of(outboxEvent));

        // ACT
        outboxEventProcessor.process(eventId);

        // ASSERT
        assertEquals(
                OutboxStatus.PUBLISHED,
                outboxEvent.getStatus()
        );
        assertNotNull(outboxEvent.getPublishedAt());

        verify(outboxRepository).findByIdForUpdate(eventId);
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

        when(outboxRepository.findByIdForUpdate(eventId))
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

        verify(outboxRepository).findByIdForUpdate(eventId);

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

        when(outboxRepository.findByIdForUpdate(eventId))
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

        verify(outboxRepository).findByIdForUpdate(eventId);

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

        when(outboxRepository.findByIdForUpdate(eventId)).
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

        verify(outboxRepository).findByIdForUpdate(eventId);

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


        when(outboxRepository.findByIdForUpdate(eventId))
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

        verify(outboxRepository).findByIdForUpdate(eventId);
        verify(objectMapper).readValue(
                payload,
                AccountCreatedEvent.class
        );

        verify(accountCreatedPublisher)
                .publish(accountCreatedEvent);
    }

    @Test
    void process_shouldPublishTransferCompletedEvent() throws JacksonException {
        UUID eventUuid = UUID.fromString(
                "45454545-4545-4545-4545-454545454545"
        );
        String eventId = eventUuid.toString();
        String payload = "{\"sourceAccountNumber\":\"TR100\"}";

        OutboxEvent outboxEvent = new OutboxEvent(
                eventId,
                "TRANSFER_COMPLETED",
                "TR100",
                payload
        );
        TransferCompletedEvent transferCompletedEvent =
                new TransferCompletedEvent(
                        eventUuid,
                        "TR100",
                        "TR200",
                        new BigDecimal("50.00"),
                        new BigDecimal("150.00"),
                        new BigDecimal("250.00"),
                        Instant.parse("2026-08-07T10:00:00Z")
                );

        when(outboxRepository.findByIdForUpdate(eventId))
                .thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue(payload, TransferCompletedEvent.class))
                .thenReturn(transferCompletedEvent);
        when(transferCompletedPublisher.publishResult(transferCompletedEvent))
                .thenReturn(new RabbitPublishResult(true, null));

        outboxEventProcessor.process(eventId);

        assertEquals(OutboxStatus.PUBLISHED, outboxEvent.getStatus());
        assertNotNull(outboxEvent.getPublishedAt());
        verify(transferCompletedPublisher).publishResult(transferCompletedEvent);
        verifyNoInteractions(accountCreatedPublisher);
    }

}
