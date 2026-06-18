package com.example.bankservice;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class BankController {
    private final BankService bankService;

    public BankController(BankService bankService){
        this.bankService  = bankService;
    }

    @PostMapping("/api/accounts/{accountNumber}/withdraw")

    public ResponseEntity<BankAccountResponseDto> withdraw(@PathVariable String accountNumber, @RequestParam double amount){
        BankAccount account = bankService.withdraw(accountNumber,amount);
        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setName(account.getName());
        dto.setBalance(account.getBalance());
        dto.setAccountNumber(account.getAccountNumber());
        return ResponseEntity.ok(dto);
    }
    @PostMapping("/api/accounts/{accountNumber}/deposit")
    public ResponseEntity<BankAccountResponseDto> deposit(@PathVariable String accountNumber, @RequestParam double depositAmount){
        BankAccount account = bankService.deposit(accountNumber, depositAmount);
        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setBalance(account.getBalance());
        dto.setName(account.getName());
        dto.setAccountNumber(account.getAccountNumber());



        return ResponseEntity.ok(dto);
    }

    @PostMapping("/api/accounts/createAccount")

    public ResponseEntity<BankAccountResponseDto> createAccount(@RequestParam String name, @RequestParam String AccountNumber){
        BankAccount account = bankService.createAccount(name, AccountNumber);
        BankAccountResponseDto dto = new BankAccountResponseDto();
        dto.setName(account.getName());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setBalance(account.getBalance());
        return  ResponseEntity.ok(dto);
    }

}

