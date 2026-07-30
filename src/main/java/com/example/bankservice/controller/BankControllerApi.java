package com.example.bankservice.controller;

import com.example.bankservice.dto.BankAccountResponseDto;
import com.example.bankservice.dto.CreateAccountRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@RequestMapping("/api/accounts")
@SecurityRequirement(name = "basicAuth")
@Tag(
        name = "Bank Accounts",
        description = "Bank account creation, query and money operations"
)
public interface BankControllerApi {

    @PostMapping("/{accountNumber}/withdraw")
    ResponseEntity<BankAccountResponseDto> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount,
            Authentication authentication
    );

    @PostMapping("/{accountNumber}/deposit")
    ResponseEntity<BankAccountResponseDto> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal depositAmount,
            Authentication authentication
    );

    @PostMapping("/createAccount")
    @Operation(
            summary = "Create bank account",
            description = "It creates a bank account for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request data is invalid"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Account number already exists"
            )
    })
    ResponseEntity<BankAccountResponseDto> createAccount(
            @RequestBody @Valid CreateAccountRequestDto requestDto,
            Authentication authentication
    );

    @GetMapping("/{accountNumber}/getAccount")
    ResponseEntity<BankAccountResponseDto> getAccount(
            @PathVariable String accountNumber,
            Authentication authentication
    );

    @DeleteMapping("/{accountNumber}")
    ResponseEntity<Void> deleteAccount(
            @PathVariable String accountNumber,
            Authentication authentication
    );

    @GetMapping("/all")
    ResponseEntity<Page<BankAccountResponseDto>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @GetMapping("/search")
    ResponseEntity<List<BankAccountResponseDto>> getAccountsWithBalanceGreaterThan(
            @RequestParam BigDecimal minBalance,
            Authentication authentication
    );

    @DeleteMapping("/deleteuser/{userName}")
    ResponseEntity<Void> deleteUser(@PathVariable String userName);

    @PostMapping("/createAccounts")
    ResponseEntity<List<BankAccountResponseDto>> createAccounts(
            @RequestBody List<@Valid CreateAccountRequestDto> requestDtos,
            Authentication authentication
    );
}
