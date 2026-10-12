package com.banca.cuentas.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;

public enum TipoMovimiento {
    DEPOSITO("Deposito"),
    RETIRO("Retiro");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    @JsonValue
    public String getDescripcion() {
        return descripcion;
    }

    public static TipoMovimiento desde(BigDecimal valor) {
        return valor.signum() >= 0 ? DEPOSITO : RETIRO;
    }
}
