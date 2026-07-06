package com.example.bankservice.service;

import com.example.bankservice.dto.AuthenticateDto;
import com.example.bankservice.dto.LoginRequestDto;
import com.example.bankservice.dto.RegisterRequestDto;
import com.example.bankservice.enums.Role;
import com.example.bankservice.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.bankservice.entity.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service


public class AuthService {
    public static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder){
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthenticateDto register(RegisterRequestDto request){
        if(appUserRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("Name already exists.");
        }
        AppUser appUser = new AppUser();
        appUser.setUsername(request.getUsername());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(Role.CUSTOMER);
        appUserRepository.save(appUser);
        return new AuthenticateDto("Registration successful",request.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthenticateDto login(LoginRequestDto requestDto){
        AppUser appUser = appUserRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> {
                    log.warn("Incorrect username");
                    return  new IllegalArgumentException("Incorrect username or password");
                });
        if(!passwordEncoder.matches(
                requestDto.getPassword(),
                appUser.getPassword()
        )){
            throw new IllegalArgumentException("Incorrect username or password");
        }
        log.info("Login successfully");
        return new AuthenticateDto("Login Successful.", requestDto.getUsername());
    }

}
