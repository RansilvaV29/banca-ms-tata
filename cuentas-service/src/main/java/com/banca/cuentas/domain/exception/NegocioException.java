package com.banca.cuentas.domain.exception;

import lombok.Getter;


@Getter
public abstract class NegocioException extends RuntimeException {

    private final String codigo;

    protected NegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
}
