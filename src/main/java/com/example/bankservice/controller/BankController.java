package com.example.bankservice;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class BankController {
    private final BankService bankService;


    public BankController(BankService bankService){                           //buradaki constructor injection
        this.bankService  = bankService;
    }

    @PostMapping("/api/accounts/{accountNumber}/withdraw")

    public ResponseEntity<BankAccountResponseDto> withdraw(@PathVariable String accountNumber, @RequestParam double amount){
        BankAccountResponseDto account = bankService.withdraw(accountNumber,amount);
        return ResponseEntity.ok(account);
    }
    @PostMapping("/api/accounts/{accountNumber}/deposit")
    public ResponseEntity<BankAccountResponseDto> deposit(@PathVariable String accountNumber, @RequestParam double depositAmount) throws InvalidAmountException {
        BankAccountResponseDto account = bankService.deposit(accountNumber, depositAmount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/api/accounts/createAccount")

    public ResponseEntity<BankAccountResponseDto> createAccount(@RequestBody CreateAccountRequestDto request){
        BankAccountResponseDto account = bankService.createAccount(request.getName(), request.getAccountNumber());
        return  ResponseEntity.ok(account);
    }

}

