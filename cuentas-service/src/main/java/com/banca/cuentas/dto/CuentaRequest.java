package com.banca.cuentas.dto;

import com.banca.cuentas.domain.model.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CuentaRequest(

        @NotBlank
        @Pattern(regexp = "^\\d{6,20}$", message = "debe contener entre 6 y 20 dígitos")
        String numeroCuenta,

        @NotNull
        TipoCuenta tipoCuenta,

        @NotNull
        @DecimalMin(value = "0.00", message = "no puede ser negativo")
        @Digits(integer = 13, fraction = 2)
        BigDecimal saldoInicial,

        Boolean estado,

        @NotBlank @Size(max = 50)
        String clienteId
) {
}
