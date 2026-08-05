package com.example.bankservice.repository;

import com.example.bankservice.entity.BankAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.List;
import  java.util.Optional;

public interface BankRepo extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    Optional<BankAccount> findByAccountNumberAndAppUserUsername(String accountNumber, String username);

    List<BankAccount> findByBalanceGreaterThanAndAppUserUsername(BigDecimal minBalance, String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select account
            from BankAccount account
            where account.accountNumber = :accountNumber
            and account.appUser.username = :username""")

    Optional<BankAccount> findOwnedAccountForUpdate(
            @Param("accountNumber") String accountNumber,
            @Param("username") String username
            );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select account
        from BankAccount account
        where account.accountNumber = :accountNumber
        """)
    Optional<BankAccount> findAccountForUpdate(
            @Param("accountNumber") String accountNumber
    );
}

