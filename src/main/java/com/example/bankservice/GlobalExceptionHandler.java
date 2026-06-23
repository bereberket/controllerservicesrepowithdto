package com.example.bankservice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException e){
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
