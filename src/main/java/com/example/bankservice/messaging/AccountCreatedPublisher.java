package com.example.bankservice.messaging;

import com.example.bankservice.config.RabbitMqConfig;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

@Component
public class AccountCreatedPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(AccountCreatedPublisher.class);

    public AccountCreatedPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public RabbitPublishResult publish(AccountCreatedEvent createdEvent) {

        CorrelationData correlationData =
                new CorrelationData(createdEvent.eventId().toString());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.BANK_EVENTS_EXCHANGE,
                    RabbitMqConfig.ACCOUNT_CREATED_ROUTING_KEY,
                    createdEvent,
                    correlationData
            );
            CorrelationData.Confirm confirm =
                    correlationData.getFuture()
                            .get(5, TimeUnit.SECONDS);

            ReturnedMessage returnedMessage = correlationData.getReturned();

            if (returnedMessage != null) {
                String errorMessage = String.format("Message couldn't routed. Exchange: %s, routing key: %s, reply code: %s,, reply text: %s",
                        returnedMessage.getExchange(),
                        returnedMessage.getRoutingKey(),
                        returnedMessage.getReplyCode(),
                        returnedMessage.getReplyText()
                );
                log.error("{}. Correlation Id: {}", errorMessage, correlationData.getId());

                return new RabbitPublishResult(false, errorMessage);

            }
            if (!confirm.ack()) {
                String errorMessage = "Rabbit rejected message: " + confirm.reason();
                log.error("{} Correlation Id: {}", errorMessage, correlationData.getId());

                return new RabbitPublishResult(false, errorMessage);
            }
            log.info("Rabbit accepted message. Correlation ID: {}", correlationData.getId());
                return new RabbitPublishResult(true, null);
        }catch (ExecutionException | TimeoutException exception){
            log.error("Publisher confirm could not be recevied.Correlation ID: {}", correlationData.getId(),exception);

            return new RabbitPublishResult(false, exception.getMessage());
        }catch (RuntimeException exception){
            log.error("RabbitMQ message could not send. Correlation ID: {}", correlationData.getId(),exception);
            return new RabbitPublishResult(false, exception.getMessage());
        }catch (InterruptedException exception){
            Thread.currentThread().interrupt();

            return new RabbitPublishResult(
                    false,
                    "Waiting for publisher confirm was interrupted"
            );
        }


    }
}
