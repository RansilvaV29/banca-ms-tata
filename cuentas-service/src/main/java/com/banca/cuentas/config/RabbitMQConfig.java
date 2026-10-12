package com.banca.cuentas.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_CLIENTES = "banca.clientes.exchange";
    public static final String COLA_EVENTOS_CLIENTE = "cuentas.clientes.eventos";
    public static final String DLX = "banca.clientes.dlx";
    public static final String DLQ = "cuentas.clientes.eventos.dlq";

    @Bean
    public TopicExchange clientesExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_CLIENTES).durable(true).build();
    }

    @Bean
    public DirectExchange clientesDeadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean
    public Queue colaEventosCliente() {
        return QueueBuilder.durable(COLA_EVENTOS_CLIENTE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ)
                .build();
    }

    @Bean
    public Queue colaEventosClienteDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding bindingEventosCliente(@Qualifier("colaEventosCliente") Queue cola,
                                         TopicExchange clientesExchange) {
        return BindingBuilder.bind(cola).to(clientesExchange).with("cliente.#");
    }

    @Bean
    public Binding bindingDlq(@Qualifier("colaEventosClienteDlq") Queue dlq,
                              DirectExchange clientesDeadLetterExchange) {
        return BindingBuilder.bind(dlq).to(clientesDeadLetterExchange).with(DLQ);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
