package com.banca.clientes.domain.exception;

public class RecursoNoEncontradoException extends NegocioException {

    public RecursoNoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static RecursoNoEncontradoException cliente(String clienteId) {
        return new RecursoNoEncontradoException("CLIENTE_NO_ENCONTRADO",
                "No existe un cliente con clienteId '" + clienteId + "'");
    }
}
