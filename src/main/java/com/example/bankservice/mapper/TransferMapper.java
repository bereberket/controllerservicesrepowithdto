package com.example.bankservice.mapper;

import com.example.bankservice.dto.TransferResponseDto;
import com.example.bankservice.entity.BankAccount;

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
}
