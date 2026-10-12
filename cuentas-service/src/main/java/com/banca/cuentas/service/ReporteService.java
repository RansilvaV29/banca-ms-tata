package com.banca.cuentas.service;

import com.banca.cuentas.dto.reporte.EstadoCuentaResponse;
import com.banca.cuentas.dto.reporte.MovimientoReporteResponse;
import com.banca.cuentas.dto.reporte.RangoFechas;

import java.util.List;

public interface ReporteService {

    EstadoCuentaResponse estadoCuenta(String clienteId, RangoFechas rango);

    List<MovimientoReporteResponse> movimientosPlano(String clienteId, RangoFechas rango);
}
