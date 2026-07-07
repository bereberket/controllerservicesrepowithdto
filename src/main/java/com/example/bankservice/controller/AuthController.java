package com.example.bankservice.controller;

import com.example.bankservice.dto.AuthenticateDto;
import com.example.bankservice.dto.LoginRequestDto;
import com.example.bankservice.dto.RegisterRequestDto;
import com.example.bankservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<List<AuthenticateDto>> register(
            @RequestBody List< @Valid RegisterRequestDto> registerRequestDto
    ) {
        List<AuthenticateDto> responses = registerRequestDto.stream()
                .map(authService::register)
                .toList();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responses);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticateDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto
    ) {
        AuthenticateDto authenticateDto =
                authService.login(loginRequestDto);

        return ResponseEntity.ok(authenticateDto);
    }
}