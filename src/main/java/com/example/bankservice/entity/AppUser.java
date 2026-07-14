package com.example.bankservice.entity;

import com.example.bankservice.enums.ActiveSituation;
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

    @OneToMany(
            mappedBy = "appUser",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    private List<BankAccount> accounts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "CurrentState", nullable = false)
    private ActiveSituation state;


    @Column(name = "user_name", nullable = false, unique = true )
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "Role",nullable = false)
    private Role role;


    
    



}
