package com.example.bankservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Table(name = "transfer_record")
public class TransferRecord {

    @Id
    @Column(name = "transfer_id", length = 36, nullable = false, updatable = false)
    private String transferId;

    @Column(name = "request_id", length = 36, nullable = false, unique = true, updatable = false)
    private String requestId;

    @Column(name = "performed_by", nullable = false, updatable = false)
    private String performedBy;

    @Column(name = "source_account_number", nullable = false, updatable = false)
    private String sourceAccountNumber;

    @Column(name = "target_account_number", nullable = false, updatable = false)
    private String targetAccountNumber;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "source_balance_after", precision = 19, scale = 2, nullable = false, updatable = false)
    private BigDecimal sourceBalanceAfter;

    @Column(name = "target_balance_after", precision = 19, scale = 2, nullable = false, updatable = false)
    private BigDecimal targetBalanceAfter;

    @Column(name = "status", length = 20, nullable = false, updatable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TransferRecord() {
    }

    public TransferRecord(
            String transferId,
            String requestId,
            String performedBy,
            String sourceAccountNumber,
            String targetAccountNumber,
            BigDecimal amount,
            BigDecimal sourceBalanceAfter,
            BigDecimal targetBalanceAfter,
            Instant createdAt
    ) {
        this.transferId = transferId;
        this.requestId = requestId;
        this.performedBy = performedBy;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.sourceBalanceAfter = sourceBalanceAfter;
        this.targetBalanceAfter = targetBalanceAfter;
        this.status = "COMPLETED";
        this.createdAt = createdAt;
    }
}
