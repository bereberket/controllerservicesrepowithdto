package com.example.bankservice.entity;

import com.example.bankservice.enums.Role;
import com.example.bankservice.repository.BankRepo;
import jakarta.persistence.*;



import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

@Entity
@Table

public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "appUser")
    private List<BankAccount> accounts = new ArrayList<>();

    @Column(name = "user_name",nullable = false, unique = true )
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role")
    private Role role;
    
    



}
