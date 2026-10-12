package com.banca.clientes.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** Formato único de error para toda la API. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String codigo,
        String mensaje,
        String path,
        List<String> detalles
) {
}
