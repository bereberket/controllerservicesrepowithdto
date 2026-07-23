package com.example.bankservice.repository;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository
    extends JpaRepository<OutboxEvent, String>{
        List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc( //read part of part find/top50/ByStatus/OrderByCreatedAtAsc
                OutboxStatus status
                );
    }

