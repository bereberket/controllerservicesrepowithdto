package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.messaging.AccountCreatedEvent;
import com.example.bankservice.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxWorkerTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private OutboxEventProcessor outboxEventProcessor;

    @InjectMocks
    private OutboxWorker outboxWorker;

    @Test
    void publishedPendingEvents_shouldProcessEveryPendingEvent(){


        String eventId = "23232323-2323-2323-2323-232323232323";
        String eventId2 = "56565656-5656-5656-5656-565656565656";
        OutboxEvent firstEvent = new OutboxEvent(
                eventId,
                "ACCOUNT_CREATED",
                "TR5689",
                "{\"accountNumber\":\"TR5689\"}"
                );
        OutboxEvent secondEvent = new OutboxEvent(
                eventId2,
                "ACCOUNT_CREATED",
                "TR3131",
                "{\"accountNumber\":\"TR3131\"}"
        );

        when(outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(firstEvent,secondEvent));

        outboxWorker.publishedPendingEvents();


        verify(outboxRepository).findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        verify(outboxEventProcessor).process(firstEvent.getEventId());
        verify(outboxEventProcessor).process(secondEvent.getEventId());


    }



    @Test
    void publishedPendingEvents_shouldDoNothing_whenEventNotExist(){


        when(outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of());


        //there is process method in publishedPendingEvents since no need for process
        outboxWorker.publishedPendingEvents();

        verify(outboxRepository).findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        verifyNoInteractions(outboxEventProcessor);
    }

    @Test
    void publishedPendingEvents_shouldContinue_whenOneEventFailed(){
        String eventId = "26262626-2626-2626-2626-2626262626";
        String eventId2 = "13131313-1313-1313-1313-1313131313";
        OutboxEvent firstEvent = new OutboxEvent(
                eventId,
                "ACCOUNT_CREATED",
                "TR1010",
                "{\"accountNumber\":\"TR1010\"}"
        );
        OutboxEvent secondEvent = new OutboxEvent(
                eventId2,
                "ACCOUNT_CREATED",
                "TR1011",
                "{\"accountNumber\":\"TR1011\"}"
        );
        when(outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(firstEvent,secondEvent));

        doThrow(IllegalStateException.class).when(outboxEventProcessor)
                        .process(eventId);

        assertDoesNotThrow(()->outboxWorker.publishedPendingEvents());


        verify(outboxEventProcessor).process(eventId);
        verify(outboxEventProcessor).process(eventId2);
    }






}
