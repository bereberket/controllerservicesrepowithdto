package com.example.bankservice.messaging;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Component
public class AccountCreatedEventHandler {
    private final AccountCreatedPublisher accountCreatedPublisher;

    public AccountCreatedEventHandler(AccountCreatedPublisher accountCreatedPublisher){
        this.accountCreatedPublisher = accountCreatedPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AccountCreatedEvent event){
        accountCreatedPublisher.publish(event);
    }
}
