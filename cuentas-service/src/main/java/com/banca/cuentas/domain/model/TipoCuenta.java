package com.banca.cuentas.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum TipoCuenta {
    AHORROS("Ahorros"),
    CORRIENTE("Corriente");

    private final String descripcion;

    TipoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    @JsonValue
    public String getDescripcion() {
        return descripcion;
    }


    @JsonCreator
    public static TipoCuenta desde(String valor) {
        if (valor == null) {
            return null;
        }
        return switch (valor.trim().toUpperCase(Locale.ROOT)) {
            //el caso de uso dice Ahorro y Ahorros asi que tolera las dos entradas
            case "AHORRO", "AHORROS" -> AHORROS;
            case "CORRIENTE" -> CORRIENTE;
            default -> throw new IllegalArgumentException(
                    "Tipo de cuenta no válido: '" + valor + "'. Valores permitidos: Ahorros, Corriente");
        };
    }
}
