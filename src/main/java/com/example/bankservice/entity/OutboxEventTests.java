package com.example.bankservice.entity;

import com.example.bankservice.enums.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OutboxEventTests {


    @Test
    void prepareForRetry_shouldResetEvent_whenStatusFailed(){

        OutboxEvent event = createEvent();

        event.registerFailure("RabbitMQ connection failed",1);

        assertEquals(OutboxStatus.FAILED, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertNotNull(event.getLastError());

        //ACT
        event.prepareForRetry();

        assertEquals(OutboxStatus.PENDING,event.getStatus());
        assertEquals(0,event.getRetryCount());
        assertNull(event.getLastError());
        assertNull(event.getPublishedAt());
    }


    @Test
    void prepareForRetry_shouldThrowException_whenStatusIsPending(){
        OutboxEvent event = createEvent();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, event::prepareForRetry
        );

        assertEquals("Only failed outbox events can be retried.",exception.getMessage());

        assertEquals(OutboxStatus.PENDING, event.getStatus());
    }

    @Test
    void prepareForRetry_shouldThrowException_whenStatusIsPublished(){
        OutboxEvent event = createEvent();
        event.markPublished();

        assertThrows(IllegalStateException.class,
                event::prepareForRetry);

        assertEquals(OutboxStatus.PUBLISHED,event.getStatus());
        assertNotNull(event.getPublishedAt());
    }

    private OutboxEvent createEvent(){
        return new OutboxEvent(
                "11111111-1111-1111-1111-111111111111",
                "ACCOUNT_CREATED",
                "TR99001",
                "{\"accountNumber\":\"TR99001\"}"
        );
    }
}
