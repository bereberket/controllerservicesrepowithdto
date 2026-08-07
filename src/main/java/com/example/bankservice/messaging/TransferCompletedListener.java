package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import com.example.bankservice.entity.ProcessedMessage;
import com.example.bankservice.repository.ProcessedMessageRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Data
public class TransferCompletedListener {
    private static final Logger log = LoggerFactory.getLogger(TransferCompletedListener.class);
    private final ProcessedMessageRepository processedMessageRepository;

    @Transactional
    @RabbitListener(queues = RabbitMqConfig.TRANSFER_COMPLETED_QUEUE)
    public void handle(TransferCompletedEvent transferCompletedEvent){
        String eventId = transferCompletedEvent.eventId().toString();
        if(processedMessageRepository.existsById(eventId)){
            log.warn("This event already exists. Event Id: {}", eventId);
            return;
        }
        log.info(
                "Transfer event received. Source Account: {}, Target Account: {}",transferCompletedEvent.sourceAccountNumber(),transferCompletedEvent.targetAccountNumber());

        ProcessedMessage processedMessage =
                new ProcessedMessage(
                        eventId,
                        "TRANSFER_COMPLETED",
                        Instant.now()
                );

        processedMessageRepository.save(processedMessage);

        log.info("Transfer completed process successfully.Source Account: {}, Target Account: {}",transferCompletedEvent.sourceAccountNumber(),transferCompletedEvent.targetAccountNumber());
    }
}
