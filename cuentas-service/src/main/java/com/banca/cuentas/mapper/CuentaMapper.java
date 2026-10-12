package com.banca.cuentas.mapper;

import com.banca.cuentas.domain.model.Cuenta;
import com.banca.cuentas.dto.CuentaResponse;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public CuentaResponse toResponse(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getSaldoDisponible(),
                cuenta.getEstado(),
                cuenta.getClienteId()
        );
    }
}
