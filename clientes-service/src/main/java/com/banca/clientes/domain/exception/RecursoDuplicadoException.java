package com.banca.clientes.domain.exception;

public class RecursoDuplicadoException extends NegocioException {

    public RecursoDuplicadoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
