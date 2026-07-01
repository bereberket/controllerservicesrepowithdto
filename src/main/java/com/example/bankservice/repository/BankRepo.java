package com.example.bankservice.repository;

import com.example.bankservice.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import  java.util.Optional;

public interface BankRepo extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByName(String name);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    Optional<BankAccount> findByBalanceGreaterThan(double minBalance);

}

