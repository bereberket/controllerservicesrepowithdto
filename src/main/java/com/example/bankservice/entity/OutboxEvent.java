package com.example.bankservice.entity;

import com.example.bankservice.enums.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;

import javax.swing.tree.AbstractLayoutCache;
import java.time.Instant;

@Getter
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    @Column(name = "event_id", length = 36, nullable = false, updatable = false)
    private String eventId;

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Column(name = "aggregate_id", length = 255, nullable = false)
    private String aggregateId;    //which account belongs to

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;  //Json Type of event

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    protected OutboxEvent() {
    }

    public OutboxEvent(
            String eventId,
            String eventType,
            String aggregateId,
            String payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
        this.retryCount = 0;
    }

    public void markPublished(){ //assigned to constants
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
    }
    public void registerFailure(
            String errorMessage,
            int maximumRetryCount
    ){
        this.retryCount++;

        if(errorMessage == null || errorMessage.isBlank()){
            this.lastError = "Unknown RabbitMQ message ";
        }else{
            this.lastError = errorMessage.substring(0, Math.min(errorMessage.length(), 1000));
        }
        if(this.retryCount >= maximumRetryCount){
            this.status = OutboxStatus.FAILED;
        }
    }

    public void prepareForRetry(){
        if(this.status != OutboxStatus.FAILED){   //metodun çalıştığı statü alanı    --->>> if(outboxEvent.getStatus() != OutboxStatus.FAILED)
            throw new IllegalStateException(
                    "Only failed outbox events can be retried."
            );
        }
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.lastError = null;
        this.publishedAt = null;
    }
}