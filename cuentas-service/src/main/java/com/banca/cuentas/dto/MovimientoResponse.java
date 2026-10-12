package com.banca.cuentas.dto;

import com.banca.cuentas.domain.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        LocalDateTime fecha,
        String numeroCuenta,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo
) {
}
