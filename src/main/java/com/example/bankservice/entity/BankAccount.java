package com.example.bankservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter

@Entity
@Table

public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private AppUser appUser;



    @Column(name = "NAMELY", nullable = false)
    private String name;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false,unique = true)
    private String accountNumber;

    @Override
    public boolean equals(Object o){
        if(this == o){ return true;}
        if(o == null || getClass() != o.getClass()){
            return false;
        }
        BankAccount that =(BankAccount)o;
        return Objects.equals(accountNumber, that.accountNumber);

    }

    @Override
    public int hashCode(){
        return Objects.hash(accountNumber);
    }



}



