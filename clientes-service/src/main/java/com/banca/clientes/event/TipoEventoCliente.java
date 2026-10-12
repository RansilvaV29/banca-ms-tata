package com.banca.clientes.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoEventoCliente {
    CLIENTE_CREADO("cliente.creado"),
    CLIENTE_ACTUALIZADO("cliente.actualizado"),
    CLIENTE_ELIMINADO("cliente.eliminado");

    private final String routingKey;
}
