package com.example.bankservice.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {
    public static final String ACCOUNT_CREATED_QUEUE =
            "account.created.queue";

    public static final String BANK_EVENTS_EXCHANGE =
            "bank.events.exchange";

    public static final String ACCOUNT_CREATED_ROUTING_KEY =
            "account.created";

    @Bean
    public Queue accountCreatedQueue(){
        return QueueBuilder
                .durable(ACCOUNT_CREATED_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange bankEventsExchange(){
        return new DirectExchange(BANK_EVENTS_EXCHANGE);
    }

    @Bean
    public Binding accountCreatedBinding(Queue accountCreatedQueue, DirectExchange bankEventsExchange){
        return BindingBuilder
                .bind(accountCreatedQueue)
                .to(bankEventsExchange)
                .with(ACCOUNT_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new JacksonJsonMessageConverter();
    }




}
