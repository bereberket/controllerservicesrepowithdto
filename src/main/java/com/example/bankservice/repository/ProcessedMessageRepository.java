package com.example.bankservice.repository;

import com.example.bankservice.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository
    extends JpaRepository<ProcessedMessage, String>{

}
