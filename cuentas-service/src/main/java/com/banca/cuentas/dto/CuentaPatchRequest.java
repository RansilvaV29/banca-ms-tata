package com.banca.cuentas.dto;

import com.banca.cuentas.domain.model.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record CuentaPatchRequest(

        TipoCuenta tipoCuenta,

        @DecimalMin(value = "0.00", message = "no puede ser negativo")
        @Digits(integer = 13, fraction = 2)
        BigDecimal saldoInicial,

        Boolean estado
) {
}
