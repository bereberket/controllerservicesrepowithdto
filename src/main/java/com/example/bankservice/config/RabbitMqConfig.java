package com.example.bankservice.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public static final String ACCOUNT_CREATED_DLQ =
            "account.created.dlq";
    public static final String ACCOUNT_CREATED_DLX =
            "account.created.dlx";
    public static final String ACCOUNT_CREATED_FAILED_ROUTING_KEY =
            "account.created.failed";




    @Bean
    public Queue accountCreatedQueue(){
        return QueueBuilder
                .durable(ACCOUNT_CREATED_QUEUE)
                .deadLetterExchange(ACCOUNT_CREATED_DLX)
                .deadLetterRoutingKey(ACCOUNT_CREATED_FAILED_ROUTING_KEY)
                .build();
    }
    @Bean
    public Queue accountCreatedDeadLetterQueue(){
        return QueueBuilder
                .durable(ACCOUNT_CREATED_DLQ).build();
    }

    @Bean
    public DirectExchange bankEventsDeadLetterExchange(){
        return new DirectExchange(ACCOUNT_CREATED_DLX);
    }

    @Bean
    public DirectExchange bankEventsExchange(){
        return new DirectExchange(BANK_EVENTS_EXCHANGE);
    }

    @Bean
    public Binding accountCreatedDeadLetterBinding(
            @Qualifier("accountCreatedDeadLetterQueue")
            Queue deadLetterQueue,

            @Qualifier("bankEventsDeadLetterExchange")
            DirectExchange deadLetterExchange
    ){
                return BindingBuilder
                        .bind(deadLetterQueue)
                        .to(deadLetterExchange)
                        .with(ACCOUNT_CREATED_FAILED_ROUTING_KEY);
    }





    @Bean
    public Binding accountCreatedBinding(
            @Qualifier("accountCreatedQueue")
            Queue accountCreatedQueue,

            @Qualifier("bankEventsExchange")
            DirectExchange bankEventsExchange
    ){
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
