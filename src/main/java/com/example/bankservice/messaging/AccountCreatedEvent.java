package com.example.bankservice.messaging;

import java.time.Instant;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID eventId,
        String accountNumber,
        String username,
        String accountName,
        Instant createdAt
) {

}
