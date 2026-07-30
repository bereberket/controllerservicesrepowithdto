package com.example.bankservice.controller;


import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.service.AuthService;
import com.example.bankservice.service.BankService;
import com.example.bankservice.dto.CreateAccountRequestDto;
import com.example.bankservice.exception.InvalidAmountException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.AsyncWebRequestInterceptor;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/accounts/")
public class BankController {
    private final BankService bankService;


    public BankController(BankService bankService) {                           // constructor injection
        this.bankService = bankService;
    }

    @PostMapping("{accountNumber}/withdraw")

    public ResponseEntity<BankAccountResponseDto> withdraw(@PathVariable String accountNumber, @RequestParam BigDecimal amount,Authentication authentication) {

        return ResponseEntity.ok(bankService.withdraw(accountNumber, amount,authentication.getName() ));
    }

    @PostMapping("{accountNumber}/deposit")
    public ResponseEntity<BankAccountResponseDto> deposit(@PathVariable String accountNumber, @RequestParam BigDecimal depositAmount,Authentication authentication){

        return ResponseEntity.ok(bankService.deposit(accountNumber,depositAmount,authentication.getName()));
    }

    @PostMapping("createAccount")
    public ResponseEntity<BankAccountResponseDto> createAccount(@RequestBody @Valid CreateAccountRequestDto requestDto,Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bankService.createAccount(requestDto,authentication.getName()));

    }

    @GetMapping("{accountNumber}/getAccount")
    public ResponseEntity<BankAccountResponseDto> getAccount(@PathVariable String accountNumber,Authentication authentication) {

        return ResponseEntity.ok(bankService.getAccount(accountNumber, authentication.getName()));
    }

    @DeleteMapping("{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber,Authentication authentication) {
        bankService.deleteAccount(accountNumber,authentication.getName());
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/all")
    public ResponseEntity<Page<BankAccountResponseDto>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ){
        return ResponseEntity.ok(bankService.getAllAccounts(page,size,sortBy,direction));
    }

    @GetMapping("/search")
    public ResponseEntity<List<BankAccountResponseDto>> getAccountsWithBalanceGreaterThan(@RequestParam BigDecimal minBalance, Authentication authentication) {

        return ResponseEntity.ok(bankService.getAccountsWithBalanceGreaterThan(minBalance,authentication.getName()));

    }
    @DeleteMapping("/deleteuser/{userName}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userName){
        bankService.deleteUser(userName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("createAccounts")
    public ResponseEntity<List<BankAccountResponseDto>> createAccounts(
            @RequestBody List<@Valid CreateAccountRequestDto> requestDtos,
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


}

