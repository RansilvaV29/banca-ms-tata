package com.banca.cuentas.dto;

import com.banca.cuentas.domain.model.TipoCuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoDisponible,
        Boolean estado,
        String clienteId
) {
}
