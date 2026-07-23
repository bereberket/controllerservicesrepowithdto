package com.example.bankservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
@Getter

@Entity
@Table(name = "processed_message")
public class ProcessedMessage {

    @Id
    @Column(name = "event_id", length = 36,nullable = false,updatable = false)
    private String eventId;

    @Column(name = "event_type", length=100,nullable = false)
    private String eventType;


    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected ProcessedMessage(){

    }
    public ProcessedMessage(
            String eventId,
            String eventType,
            Instant processedAt
    ){
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }


}
