package com.banca.clientes.event;

import com.banca.clientes.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicar(ClienteEvent evento) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_CLIENTES, evento.tipo().getRoutingKey(), evento);
            log.info("Evento publicado: tipo={}, clienteId={}, eventoId={}",
                    evento.tipo(), evento.clienteId(), evento.eventoId());
        } catch (AmqpException ex) {
            log.error("No se pudo publicar el evento {} del cliente {}: {}",
                    evento.tipo(), evento.clienteId(), ex.getMessage());
        }
    }
}
