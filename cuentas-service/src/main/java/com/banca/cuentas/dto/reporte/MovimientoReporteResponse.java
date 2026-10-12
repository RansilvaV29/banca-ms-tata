package com.banca.cuentas.dto.reporte;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;

@JsonPropertyOrder({"Fecha", "Cliente", "Numero Cuenta", "Tipo", "Saldo Inicial", "Estado",
        "Movimiento", "Saldo Disponible"})
public record MovimientoReporteResponse(
        @JsonProperty("Fecha") String fecha,
        @JsonProperty("Cliente") String cliente,
        @JsonProperty("Numero Cuenta") String numeroCuenta,
        @JsonProperty("Tipo") String tipo,
        @JsonProperty("Saldo Inicial") BigDecimal saldoInicial,
        @JsonProperty("Estado") Boolean estado,
        @JsonProperty("Movimiento") BigDecimal movimiento,
        @JsonProperty("Saldo Disponible") BigDecimal saldoDisponible
) {
}
