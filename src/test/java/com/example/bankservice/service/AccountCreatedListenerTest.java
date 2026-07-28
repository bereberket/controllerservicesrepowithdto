package com.example.bankservice.service;

import com.example.bankservice.entity.ProcessedMessage;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.messaging.AccountCreatedListener;
import com.example.bankservice.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountCreatedListenerTest {

    @Mock
    private ProcessedMessageRepository processedMessageRepository;

    @InjectMocks
    private AccountCreatedListener accountCreatedListener;

    @Test
    void handle_shouldSaveMessage_whenEventIsNew(){
        UUID eventId = UUID.fromString("12121212-1212-1212-1212-121212121212");

        AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent(
                eventId,
                "TR1212",
                "beko",
                "Main Account",
                Instant.now()

        );

        when(processedMessageRepository.existsById(eventId.toString()))
                .thenReturn(false);

        accountCreatedListener.handle(accountCreatedEvent);

        ArgumentCaptor<ProcessedMessage> eventArgumentCaptor =
                ArgumentCaptor.forClass(ProcessedMessage.class);

        verify(processedMessageRepository).save(eventArgumentCaptor.capture());
    }

    @Test
    void handle_shouldNotSave_whenEventIdExists() throws IllegalStateException{

        UUID eventUuId = UUID.fromString("10101010-1010-1010-1010-101010101010");
        String eventId = eventUuId.toString();
        AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent(
                eventUuId,
                "TR5959",
                "ken",
                "Main Account",
                Instant.parse("2026-07-27T10:00:00Z")
        );

        when(processedMessageRepository.existsById(eventUuId.toString()))
                .thenReturn(true);
       assertDoesNotThrow(()-> accountCreatedListener.handle(accountCreatedEvent));

        verify(processedMessageRepository)
                .existsById(eventId);
        verifyNoMoreInteractions(processedMessageRepository);


    }

}
