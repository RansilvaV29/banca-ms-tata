package com.banca.cuentas.domain.exception;

public class ConflictoException extends NegocioException {

    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
