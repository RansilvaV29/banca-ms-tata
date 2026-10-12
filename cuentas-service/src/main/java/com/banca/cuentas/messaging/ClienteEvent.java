package com.banca.cuentas.messaging;

import java.time.Instant;

/**
 * Contrato del evento publicado por clientes-service. Cada microservicio tiene su propia copia
 * de la clase (no se comparte una librería) para que puedan desplegarse de forma independiente;
 * lo que se comparte es el formato JSON.
 */
public record ClienteEvent(
        String eventoId,
        TipoEventoCliente tipo,
        String clienteId,
        String nombre,
        Boolean estado,
        Instant ocurridoEn
) {
}
