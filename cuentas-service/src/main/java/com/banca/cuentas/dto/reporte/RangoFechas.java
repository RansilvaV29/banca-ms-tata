package com.banca.cuentas.dto.reporte;

import com.banca.cuentas.domain.exception.SolicitudInvalidaException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public record RangoFechas(LocalDate desde, LocalDate hasta) {

    private static final String FORMATO = "Use fecha=AAAA-MM-DD,AAAA-MM-DD (por ejemplo fecha=2022-02-01,2022-02-28)";

    public RangoFechas {
        if (desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("RANGO_FECHAS_INVALIDO",
                    "La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    public static RangoFechas parse(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new SolicitudInvalidaException("RANGO_FECHAS_INVALIDO", "El rango de fechas es obligatorio. " + FORMATO);
        }
        String[] partes = valor.split(",");
        if (partes.length != 2) {
            throw new SolicitudInvalidaException("RANGO_FECHAS_INVALIDO", "Rango de fechas mal formado. " + FORMATO);
        }
        try {
            return new RangoFechas(LocalDate.parse(partes[0].trim()), LocalDate.parse(partes[1].trim()));
        } catch (DateTimeParseException ex) {
            throw new SolicitudInvalidaException("RANGO_FECHAS_INVALIDO", "Fecha con formato inválido. " + FORMATO);
        }
    }

    public LocalDateTime inicio() {
        return desde.atStartOfDay();
    }

    public LocalDateTime finExclusivo() {
        return hasta.plusDays(1).atStartOfDay();
    }
}
