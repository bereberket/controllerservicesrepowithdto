package com.example.bankservice.controller;


import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.service.BankService;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.exception.InvalidAmountException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;


@RestController
public class BankController {
    private final BankService bankService;


    public BankController(BankService bankService) {                           // constructor injection
        this.bankService = bankService;
    }

    @PostMapping("/api/accounts/{accountNumber}/withdraw")

    public ResponseEntity<BankAccountResponseDto> withdraw(@PathVariable String accountNumber, @RequestParam double amount) {
        BankAccountResponseDto account = bankService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/api/accounts/{accountNumber}/deposit")
    public ResponseEntity<BankAccountResponseDto> deposit(@PathVariable String accountNumber, @RequestParam double depositAmount) throws InvalidAmountException {
        BankAccountResponseDto account = bankService.deposit(accountNumber, depositAmount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/api/accounts/createAccount")

    public ResponseEntity<BankAccountResponseDto> createAccount(@RequestBody CreateAccountRequestDto request) {
        BankAccountResponseDto account = bankService.createAccount(request.getName(), request.getAccountNumber());
        return ResponseEntity.ok(account);
    }

    @GetMapping("/api/accounts/{accountNumber}/getAccount")

    public ResponseEntity<BankAccountResponseDto> getAccount(@PathVariable String accountNumber) {
        BankAccountResponseDto account = bankService.getAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/api/accounts/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        bankService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/accounts/all")
    public ResponseEntity<List<BankAccountResponseDto>> getAllAccounts() {
        List<BankAccountResponseDto> accounts = bankService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/api/accounts/search")

    public ResponseEntity<List<BankAccountResponseDto>> getAccountsWithBalanceGreaterThan(@RequestParam double minBalance) {
        List<BankAccountResponseDto> accounts = bankService.getAccountsWithBalanceGreaterThan(minBalance);
        return ResponseEntity.ok(accounts);

    }
}

