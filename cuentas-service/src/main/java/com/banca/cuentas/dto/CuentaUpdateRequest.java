package com.banca.cuentas.dto;

import com.banca.cuentas.domain.model.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CuentaUpdateRequest(

        @NotNull
        TipoCuenta tipoCuenta,

        @NotNull
        @DecimalMin(value = "0.00", message = "no puede ser negativo")
        @Digits(integer = 13, fraction = 2)
        BigDecimal saldoInicial,

        @NotNull
        Boolean estado
) {
}
