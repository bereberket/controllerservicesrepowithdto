package com.example.bankservice.controller;

import com.example.bankservice.service.OutboxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/outbox")
public class OutboxAdminController {

    private final OutboxService outboxService;

    public OutboxAdminController(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @PostMapping("/{eventId}/retry")
    public ResponseEntity<Void> retryFailedEvent(
            @PathVariable String eventId
    ) {
        outboxService.requeueFailedEvent(eventId);

        return ResponseEntity.accepted().build();
    }
}