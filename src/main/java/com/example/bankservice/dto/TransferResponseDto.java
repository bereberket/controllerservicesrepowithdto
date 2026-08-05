package com.example.bankservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
@Getter
@Setter
public class TransferResponseDto {
    private String sourceAccountNumber;

    private String targetAccountNumber;

    private BigDecimal amount;

    private BigDecimal sourceAfterTransfer;

    private Instant transferredAt;

    private String message;
}
