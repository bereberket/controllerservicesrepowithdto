package com.example.bankservice.controller;


import com.example.bankservice.dto.BankAccountResponseDto;
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
import java.util.List;


@RestController
@RequestMapping("/api/accounts/")
public class BankController {
    private final BankService bankService;


    public BankController(BankService bankService) {                           // constructor injection
        this.bankService = bankService;
    }

    @PostMapping("{accountNumber}/withdraw")

    public ResponseEntity<BankAccountResponseDto> withdraw(@PathVariable String accountNumber, @RequestParam double amount) {
        BankAccountResponseDto account = bankService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("{accountNumber}/deposit")
    public ResponseEntity<BankAccountResponseDto> deposit(@PathVariable String accountNumber, @RequestParam double depositAmount) throws InvalidAmountException {
        BankAccountResponseDto account = bankService.deposit(accountNumber, depositAmount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("createAccount")

    public ResponseEntity<BankAccountResponseDto> createAccount(@Valid @RequestBody CreateAccountRequestDto request) {

        BankAccountResponseDto account = bankService.createAccount(request.getName(), request.getAccountNumber(), request.getUserName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(account);
    }

    @GetMapping("{accountNumber}/getAccount")

    public ResponseEntity<BankAccountResponseDto> getAccount(@PathVariable String accountNumber) {
        BankAccountResponseDto account = bankService.getAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        bankService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/all")
    public ResponseEntity<Page<BankAccountResponseDto>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort.Direction sortDirection;
               if(direction.equalsIgnoreCase("asc")){
                   sortDirection = Sort.Direction.ASC;
               }else if(direction.equalsIgnoreCase("desc")){
                   sortDirection = Sort.Direction.DESC;
               }else{
                   throw new IllegalArgumentException("Must be ASC or DESC");
               }

        List<String> allowedSortFields = List.of("id", "name", "balance","accountNumber");

        if(!allowedSortFields.contains(sortBy)){
            throw new IllegalArgumentException("Invalid sort parameter");
        }
        if(page<0){
            throw new IllegalArgumentException("Must be positive");
        }
        if(size<1 || size>100){
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        Pageable pageable = PageRequest.of(page,size,sortDirection);

        Page <BankAccountResponseDto> accounts =
                bankService.getAllAccounts(pageable);



        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")

    public ResponseEntity<List<BankAccountResponseDto>> getAccountsWithBalanceGreaterThan(@RequestParam double minBalance) {
        List<BankAccountResponseDto> accounts = bankService.getAccountsWithBalanceGreaterThan(minBalance);
        return ResponseEntity.ok(accounts);

    }
}

