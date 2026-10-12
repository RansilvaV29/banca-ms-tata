package com.banca.cuentas.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EstadoCuentaResponse(
        String clienteId,
        String cliente,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        List<CuentaEstado> cuentas
) {

    public record CuentaEstado(
            String numeroCuenta,
            String tipo,
            BigDecimal saldoInicial,
            Boolean estado,
            BigDecimal saldoDisponible,
            BigDecimal totalCreditos,
            BigDecimal totalDebitos,
            List<MovimientoEstado> movimientos
    ) {
    }

    public record MovimientoEstado(
            Long id,
            LocalDateTime fecha,
            String tipoMovimiento,
            BigDecimal valor,
            BigDecimal saldo
    ) {
    }
}
