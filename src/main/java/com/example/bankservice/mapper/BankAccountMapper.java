package com.example.bankservice.mapper;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.entity.BankAccount;

public class BankAccountMapper {


    public static BankAccountResponseDto toDto(BankAccount account) {
        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setName(account.getName());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        return dto;
    }
}
