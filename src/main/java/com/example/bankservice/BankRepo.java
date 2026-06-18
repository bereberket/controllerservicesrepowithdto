package com.example.bankservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import  java.util.Optional;

public interface BankRepo extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByName(String name);
    Optional<BankAccount> findByAccountNumber(String AccountNumber);


}
