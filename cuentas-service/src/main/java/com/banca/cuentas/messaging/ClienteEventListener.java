package com.banca.cuentas.messaging;

import com.banca.cuentas.config.RabbitMQConfig;
import com.banca.cuentas.service.impl.ClienteSnapshotSincronizador;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor asíncrono de los eventos de clientes. Si el procesamiento falla se reintenta
 * (configurado en application.yml) y, agotados los reintentos, el mensaje va a la DLQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteEventListener {

    private final ClienteSnapshotSincronizador sincronizador;

    @RabbitListener(queues = RabbitMQConfig.COLA_EVENTOS_CLIENTE)
    public void onClienteEvent(ClienteEvent evento) {
        log.info("Evento recibido: tipo={}, clienteId={}, eventoId={}",
                evento.tipo(), evento.clienteId(), evento.eventoId());
        sincronizador.procesar(evento);
    }
}
