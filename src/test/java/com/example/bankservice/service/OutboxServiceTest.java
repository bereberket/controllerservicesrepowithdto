package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class OutboxServiceTest {
    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxService outboxService;

    @Test
    void saveAccountCreatedEvent_shouldSavePendingOutboxEvent() throws JacksonException {
        UUID eventId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        AccountCreatedEvent accountCreatedEvent =
                new AccountCreatedEvent(
                        eventId,
                        "TR33573",
                        "Berke",
                        "Main Account",
                        Instant.parse("2026-07-24T10:00:00Z")
                );

        String payload =
                "{\"eventId\":\"11111111-1111-1111-1111-111111111111\"}";

        when(objectMapper.writeValueAsString(accountCreatedEvent))
                .thenReturn(payload);

        outboxService.saveAccountCreatedEvent(accountCreatedEvent);

        ArgumentCaptor<OutboxEvent> eventArgumentCaptor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(outboxRepository).save(eventArgumentCaptor.capture());

        OutboxEvent savedEvent = eventArgumentCaptor.getValue();

        assertEquals(eventId.toString(), savedEvent.getEventId());
        assertEquals("ACCOUNT_CREATED", savedEvent.getEventType());
        assertEquals("TR33573", savedEvent.getAggregateId());
        assertEquals(payload, savedEvent.getPayload());
        assertEquals(OutboxStatus.PENDING, savedEvent.getStatus());
        assertEquals(0, savedEvent.getRetryCount());
        assertNotNull(savedEvent.getCreatedAt());
        assertNull(savedEvent.getPublishedAt());
        assertNull(savedEvent.getLastError());
    }

    @Test
    void saveAccountCreatedEvent_shouldThrowException_whenSerializationFails()
            throws JacksonException {
        AccountCreatedEvent accountCreatedEvent =
                new AccountCreatedEvent(
                        UUID.fromString("22222222-2222-2222-2222-222222222222"),
                        "TR44556",
                        "Berke",
                        "Failed Account",
                        Instant.parse("2026-07-24T11:00:00Z")
                );

        JacksonException serializationException =
                mock(JacksonException.class);

        when(objectMapper.writeValueAsString(accountCreatedEvent))
                .thenThrow(serializationException);

        IllegalStateException thrownException =
                assertThrows(
                        IllegalStateException.class,
                        () -> outboxService.saveAccountCreatedEvent(accountCreatedEvent)
                );

        assertEquals(
                "AccountCreatedEvent could not be serialized.",
                thrownException.getMessage()
        );
        assertSame(serializationException, thrownException.getCause());

        verify(objectMapper).writeValueAsString(accountCreatedEvent);
        verify(outboxRepository, never()).save(any(OutboxEvent.class));
    }

    @Test
    void requeueFailedEvent_shouldResetFailedEventToPending() {
        String eventId = "33333333-3333-3333-3333-333333333333";

        OutboxEvent outboxEvent = new OutboxEvent(
                eventId,
                "ACCOUNT_CREATED",
                "TR56667",
                "{\"accountNumber\":\"TR56667\"}"
        );

        outboxEvent.registerFailure("RabbitMQ connection failed", 1);

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.of(outboxEvent));

        assertEquals(OutboxStatus.FAILED, outboxEvent.getStatus());

        outboxService.requeueFailedEvent(eventId);

        assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());
        assertEquals(0, outboxEvent.getRetryCount());
        assertNull(outboxEvent.getLastError());
        assertNull(outboxEvent.getPublishedAt());

        verify(outboxRepository).findById(eventId);
    }

    @Test
    void requeueFailedEvent_shouldThrowException_whenEventDoesNotExist() {
        String eventId = "44444444-4444-4444-4444-444444444444";

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> outboxService.requeueFailedEvent(eventId)
                );

        assertEquals(
                "Outbox event not found" + eventId,
                exception.getMessage()
        );

        verify(outboxRepository).findById(eventId);
    }

    @Test
    void requeueFailedEvent_shouldThrowException_whenEventIsPending() {
        String eventId = "55555555-5555-5555-5555-555555555555";

        OutboxEvent outboxEvent = new OutboxEvent(
                eventId,
                "ACCOUNT_CREATED",
                "TR66788",
                "{\"accountNumber\":\"TR66788\"}"
        );

        when(outboxRepository.findById(eventId))
                .thenReturn(Optional.of(outboxEvent));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> outboxService.requeueFailedEvent(eventId)
                );

        assertEquals(
                "Only failed outbox events can be retried.",
                exception.getMessage()
        );
        assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());

        verify(outboxRepository).findById(eventId);
    }
}
