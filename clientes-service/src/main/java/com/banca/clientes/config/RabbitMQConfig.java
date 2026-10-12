package com.banca.clientes.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * El productor solo declara el exchange. Las colas las declara cada consumidor:
 * así clientes-service no necesita saber quién escucha sus eventos (bajo acoplamiento).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_CLIENTES = "banca.clientes.exchange";

    @Bean
    public TopicExchange clientesExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_CLIENTES).durable(true).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
