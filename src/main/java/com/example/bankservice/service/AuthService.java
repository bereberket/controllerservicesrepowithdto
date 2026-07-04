package com.example.bankservice.service;

import com.example.bankservice.dto.AuthenticateDto;
import com.example.bankservice.dto.LoginRequestDto;
import com.example.bankservice.dto.RegisterRequestDto;
import com.example.bankservice.enums.Role;
import com.example.bankservice.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.bankservice.entity.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service


public class AuthService {
    public static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository){
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AuthenticateDto register(RegisterRequestDto request){
        if(appUserRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("Name already exists.");
        }
        AppUser appUser = new AppUser();
        appUser.setUsername(request.getUsername());
        appUser.setPassword(request.getPassword());
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
        if(!requestDto.getPassword().equals(appUser.getPassword())){
            throw new IllegalArgumentException("Incorrect username or password");
        }
        log.info("Login successfully");
        return new AuthenticateDto("Login Successful.", requestDto.getUsername());
    }


}
