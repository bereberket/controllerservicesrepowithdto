package com.example.bankservice.repository;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository
    extends JpaRepository<OutboxEvent, String>{
        List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc( //read part of part find/top50/ByStatus/OrderByCreatedAtAsc
                OutboxStatus status
                );

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("""
                select event
                from OutboxEvent event
                where event.eventId = :eventId
                """)
        Optional<OutboxEvent> findByIdForUpdate(
                @Param("eventId") String eventId
        );
}

