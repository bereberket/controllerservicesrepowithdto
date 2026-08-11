package com.example.bankservice.repository;

import com.example.bankservice.entity.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferRecordRepository extends JpaRepository<TransferRecord, String> {
    Optional<TransferRecord> findByRequestId(String requestId);
}
