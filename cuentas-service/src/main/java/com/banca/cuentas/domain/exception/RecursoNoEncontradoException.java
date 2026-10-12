package com.banca.cuentas.domain.exception;

public class RecursoNoEncontradoException extends NegocioException {

    public RecursoNoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static RecursoNoEncontradoException cuenta(String numeroCuenta) {
        return new RecursoNoEncontradoException("CUENTA_NO_ENCONTRADA",
                "No existe la cuenta '" + numeroCuenta + "'");
    }

    public static RecursoNoEncontradoException movimiento(Long id) {
        return new RecursoNoEncontradoException("MOVIMIENTO_NO_ENCONTRADO",
                "No existe el movimiento con id " + id);
    }

    public static RecursoNoEncontradoException cliente(String clienteId) {
        return new RecursoNoEncontradoException("CLIENTE_NO_ENCONTRADO",
                "No existe un cliente con clienteId '" + clienteId + "'");
    }
}
