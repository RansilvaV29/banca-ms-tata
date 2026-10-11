package com.banca.clientes.dto;

import com.banca.clientes.domain.model.Genero;

public record ClienteResponse(
        String clienteId,
        String nombre,
        Genero genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado
) {
}
