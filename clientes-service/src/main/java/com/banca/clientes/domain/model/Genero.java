package com.banca.clientes.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum Genero {
    MASCULINO,
    FEMENINO,
    OTRO;

    @JsonCreator
    public static Genero desde(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return Genero.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Género no válido: '" + valor + "'. Valores permitidos: MASCULINO, FEMENINO, OTRO");
        }
    }
}