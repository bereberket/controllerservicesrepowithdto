package com.example.bankservice.mapper;

import com.example.bankservice.dto.TransferResponseDto;
import com.example.bankservice.entity.BankAccount;
import com.example.bankservice.entity.TransferRecord;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferMapper {
    public static TransferResponseDto toDto(BankAccount sourceAccount, BankAccount targetAccount, BigDecimal transferredAmount){
        TransferResponseDto dto = new TransferResponseDto();

        dto.setSourceAccountNumber(sourceAccount.getAccountNumber());

        dto.setTargetAccountNumber(targetAccount.getAccountNumber());

        dto.setAmount(transferredAmount);

        dto.setSourceAfterTransfer(sourceAccount.getBalance());

        dto.setTransferredAt(Instant.now());

        dto.setMessage("Transfer completed successfully");

        return dto;
    }

    public static TransferResponseDto toDto(TransferRecord transferRecord) {
        TransferResponseDto dto = new TransferResponseDto();

        dto.setSourceAccountNumber(transferRecord.getSourceAccountNumber());
        dto.setTargetAccountNumber(transferRecord.getTargetAccountNumber());
        dto.setAmount(transferRecord.getAmount());
        dto.setSourceAfterTransfer(transferRecord.getSourceBalanceAfter());
        dto.setTransferredAt(transferRecord.getCreatedAt());
        dto.setMessage("Transfer completed successfully");

        return dto;
    }
}
