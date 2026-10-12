package com.banca.cuentas.controller;

import com.banca.cuentas.domain.exception.SolicitudInvalidaException;
import com.banca.cuentas.dto.reporte.RangoFechas;
import com.banca.cuentas.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reportes")
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @Operation(summary = "Estado de cuenta de un cliente en un rango de fechas")
    @GetMapping
    public ResponseEntity<?> estadoCuenta(
            @Parameter(description = "Rango inclusivo AAAA-MM-DD,AAAA-MM-DD", example = "2022-02-01,2022-02-28")
            @RequestParam String fecha,
            @Parameter(description = "clienteId del cliente", example = "mmontalvo")
            @RequestParam("cliente") String clienteId,
            @Parameter(description = "detallado (agrupado por cuenta) o plano (formato del enunciado)")
            @RequestParam(defaultValue = "detallado") String formato) {

        RangoFechas rango = RangoFechas.parse(fecha);
        return switch (formato.toLowerCase()) {
            case "detallado" -> ResponseEntity.ok(reporteService.estadoCuenta(clienteId, rango));
            case "plano" -> ResponseEntity.ok(reporteService.movimientosPlano(clienteId, rango));
            default -> throw new SolicitudInvalidaException("FORMATO_INVALIDO",
                    "Formato no soportado: '" + formato + "'. Use 'detallado' o 'plano'");
        };
    }
}
