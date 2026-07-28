package com.example.bankservice.repository;

import com.example.bankservice.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import  java.util.Optional;

public interface BankRepo extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByName(String name);

    Optional<BankAccount> findByAccountNumber(String accountNumber);

    List<BankAccount> findByBalanceGreaterThan(BigDecimal minBalance);

    Optional<BankAccount> findByAccountNumberAndAppUserUsername(String accountNumber, String username);

    List<BankAccount> findByBalanceGreaterThanAndAppUserUsername(BigDecimal minBalance, String username);
}

