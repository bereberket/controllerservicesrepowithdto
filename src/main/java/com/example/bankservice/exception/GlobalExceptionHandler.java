package com.example.bankservice.exception;

import com.example.bankservice.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientBalanceException(InsufficientBalanceException e
    ){
        ErrorResponseDto error = new ErrorResponseDto(
                e.getMessage(),
                400,
                "Bad Request"
        );
        return ResponseEntity.badRequest().body(error);

    }


    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountNotFoundException(AccountNotFoundException e
    ){
        ErrorResponseDto error = new ErrorResponseDto(
                e.getMessage(),
                404,
                "Not Found"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountAlreadyExistsException(AccountAlreadyExistsException e
    ){ErrorResponseDto error = new ErrorResponseDto(
            e.getMessage(),
            409,
            "Conflict"
    );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponseDto>InvalidAmountException(InvalidAmountException e
    ){
        ErrorResponseDto error = new ErrorResponseDto(
                e.getMessage(),
                400,
                "Bad Request"
        );
        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        ErrorResponseDto error = new ErrorResponseDto(
                e.getMessage(),
                400,
                "Bad Request"
        );

        return ResponseEntity.badRequest().body(error);
    }
}



