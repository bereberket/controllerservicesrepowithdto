package com.example.bankservice.controller;


import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.TransferRequestDto;
import com.example.bankservice.dto.TransferResponseDto;
import com.example.bankservice.service.BankService;
import com.example.bankservice.dto.CreateAccountRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;

@RestController
public class BankController implements BankControllerApi {
    private final BankService bankService;


    public BankController(BankService bankService) {                           // constructor injection
        this.bankService = bankService;
    }

    @Override
    public ResponseEntity<BankAccountResponseDto> withdraw(String accountNumber, BigDecimal amount, Authentication authentication) {

        return ResponseEntity.ok(bankService.withdraw(accountNumber, amount,authentication.getName() ));
    }

    @Override
    public ResponseEntity<BankAccountResponseDto> deposit(String accountNumber, BigDecimal depositAmount, Authentication authentication){

        return ResponseEntity.ok(bankService.deposit(accountNumber,depositAmount,authentication.getName()));
    }

    @Override
    public ResponseEntity<BankAccountResponseDto> createAccount(CreateAccountRequestDto requestDto, Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankService.createAccount(requestDto,authentication.getName()));

    }

    @Override
    public ResponseEntity<BankAccountResponseDto> getAccount(String accountNumber, Authentication authentication) {

        return ResponseEntity.ok(bankService.getAccount(accountNumber, authentication.getName()));
    }

    @Override
    public ResponseEntity<Void> deleteAccount(String accountNumber, Authentication authentication) {
        bankService.deleteAccount(accountNumber,authentication.getName());
        return ResponseEntity.noContent().build();
    }


    @Override
    public ResponseEntity<Page<BankAccountResponseDto>> getAllAccounts(
            int page,
            int size,
            String sortBy,
            String direction
    ){
        return ResponseEntity.ok(bankService.getAllAccounts(page,size,sortBy,direction));
    }

    @Override
    public ResponseEntity<List<BankAccountResponseDto>> getAccountsWithBalanceGreaterThan(BigDecimal minBalance, Authentication authentication) {

        return ResponseEntity.ok(bankService.getAccountsWithBalanceGreaterThan(minBalance,authentication.getName()));

    }
    @Override
    public ResponseEntity<Void> deleteUser(String userName){
        bankService.deleteUser(userName);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<BankAccountResponseDto>> createAccounts(
            List<CreateAccountRequestDto> requestDtos,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bankService.createAccounts(
                                requestDtos,
                                authentication.getName()
                        )
                );
    }
    @Override
    public ResponseEntity<TransferResponseDto> transfer(
            TransferRequestDto transferRequestDto,
            Authentication authentication,
            @RequestHeader(name = "Idempotency-Key", required = false) String requestId
    ) {

        return ResponseEntity.ok(bankService.transfer(transferRequestDto, authentication.getName(), requestId));
    }


}

