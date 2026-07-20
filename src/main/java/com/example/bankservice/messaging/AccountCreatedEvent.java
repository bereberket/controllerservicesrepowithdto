package com.example.bankservice.messaging;

import java.time.Instant;

public record AccountCreatedEvent(
        String accountNumber,
        String username,
        String accountName,
        Instant createdAt
) {

}
