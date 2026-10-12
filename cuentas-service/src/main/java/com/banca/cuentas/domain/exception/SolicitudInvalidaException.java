package com.banca.cuentas.domain.exception;

public class SolicitudInvalidaException extends NegocioException {

    public SolicitudInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}

