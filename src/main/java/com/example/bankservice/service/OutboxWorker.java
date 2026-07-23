package com.example.bankservice.service;

import com.example.bankservice.entity.OutboxEvent;
import com.example.bankservice.enums.OutboxStatus;
import com.example.bankservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxWorker {
    private static final Logger log =
            LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxRepository outboxRepository;
    private final OutboxEventProcessor outboxEventProcessor;

    public OutboxWorker(
            OutboxRepository outboxRepository,
            OutboxEventProcessor outboxEventProcessor
    ){
        this.outboxRepository = outboxRepository;
        this.outboxEventProcessor = outboxEventProcessor;
    }

     @Scheduled(fixedDelayString = "${outbox.worker.fixed-delay:5000}")
    public void publishedPendingEvents(){
        List<String> pendingEventIds = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)
                 .stream()
                 .map(OutboxEvent::getEventId)
                 .toList();

        if(pendingEventIds.isEmpty()){
            return;
        }
         log.info(
                 "Outbox worker found {} pending event(s)",
                 pendingEventIds.size()
         );

        for(String eventId : pendingEventIds){
            try{
                outboxEventProcessor.process(eventId);
            }catch (RuntimeException exception){
                log.error("Unexpected error while processing outbox event. Event ID:{}", eventId,exception);
            }
        }
     }
}
