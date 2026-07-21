package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Component
public class AccountCreatedPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(AccountCreatedPublisher.class);

    public AccountCreatedPublisher(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(AccountCreatedEvent createdEvent){

        CorrelationData correlationData =
                new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.BANK_EVENTS_EXCHANGE,
                RabbitMqConfig.ACCOUNT_CREATED_ROUTING_KEY,
                createdEvent,
                correlationData
        );
                correlationData.getFuture().whenComplete(
                        (confirm,exception) -> {
                            if(exception != null){
                                log.error(
                                        "Publisher confirm could not be received. Correlation ID: {}",
                                         correlationData.getId(),
                                        exception
                                );
                                return;
                            }
                            if(confirm.ack()){
                                log.info(
                                        "RabbitMQ accepted the message. Correlation ID:{}, reason:{}", correlationData.getId(),confirm.reason());
                            }else{
                                log.error(
                                        "RabbitMQ refuse the message. Correalation ID:{}, reason:{}",correlationData.getId(), confirm.reason());
                            }
                        }
                );
    }


}
