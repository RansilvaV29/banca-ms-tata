package com.banca.clientes.event;

import com.banca.clientes.domain.model.Cliente;

import java.time.Instant;
import java.util.UUID;


public record ClienteEvent(
        String eventoId,
        TipoEventoCliente tipo,
        String clienteId,
        String nombre,
        Boolean estado,
        Instant ocurridoEn
) {

    public static ClienteEvent de(TipoEventoCliente tipo, Cliente cliente) {
        return new ClienteEvent(
                UUID.randomUUID().toString(),
                tipo,
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getEstado(),
                Instant.now()
        );
    }
}
