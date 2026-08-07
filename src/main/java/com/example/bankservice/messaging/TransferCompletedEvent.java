package com.example.bankservice.messaging;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferCompletedEvent(
        UUID eventId,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        BigDecimal sourceBalanceAfter,
        BigDecimal targetBalanceAfter,
        Instant transferredAt

) {

}

