package com.example.bankservice.service;

import com.example.bankservice.dto.AuthenticateDto;
import com.example.bankservice.dto.RegisterRequestDto;
import com.example.bankservice.enums.ActiveSituation;
import com.example.bankservice.enums.Role;
import com.example.bankservice.repository.AppUserRepository;
import com.example.bankservice.entity.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service


public class AuthService {
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
        appUser.setState(ActiveSituation.ACTIVE);
        appUserRepository.save(appUser);
        return new AuthenticateDto("Registration successful",request.getUsername());
    }
}
