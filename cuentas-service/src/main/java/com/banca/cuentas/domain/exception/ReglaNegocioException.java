package com.banca.cuentas.domain.exception;

public class ReglaNegocioException extends NegocioException {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
