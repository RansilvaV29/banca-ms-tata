package com.banca.cuentas.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimientoRequest(

        @NotBlank
        String numeroCuenta,

        @NotNull
        @Digits(integer = 13, fraction = 2)
        BigDecimal valor
) {
}
