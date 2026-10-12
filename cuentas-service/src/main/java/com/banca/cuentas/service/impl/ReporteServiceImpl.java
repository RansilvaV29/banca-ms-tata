package com.banca.cuentas.service.impl;

import com.banca.cuentas.domain.exception.RecursoNoEncontradoException;
import com.banca.cuentas.domain.model.ClienteSnapshot;
import com.banca.cuentas.domain.model.Cuenta;
import com.banca.cuentas.domain.model.Movimiento;
import com.banca.cuentas.dto.reporte.EstadoCuentaResponse;
import com.banca.cuentas.dto.reporte.EstadoCuentaResponse.CuentaEstado;
import com.banca.cuentas.dto.reporte.EstadoCuentaResponse.MovimientoEstado;
import com.banca.cuentas.dto.reporte.MovimientoReporteResponse;
import com.banca.cuentas.dto.reporte.RangoFechas;
import com.banca.cuentas.repository.ClienteSnapshotRepository;
import com.banca.cuentas.repository.CuentaRepository;
import com.banca.cuentas.repository.MovimientoRepository;
import com.banca.cuentas.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final ClienteSnapshotRepository clienteSnapshotRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    public EstadoCuentaResponse estadoCuenta(String clienteId, RangoFechas rango) {
        ClienteSnapshot cliente = buscarCliente(clienteId);
        List<Cuenta> cuentas = cuentaRepository.findByClienteIdOrderByNumeroCuenta(clienteId);
        Map<Long, List<Movimiento>> movimientosPorCuenta = movimientosPorCuenta(clienteId, rango);

        List<CuentaEstado> detalle = cuentas.stream()
                .map(cuenta -> construirCuentaEstado(cuenta, movimientosPorCuenta.getOrDefault(cuenta.getId(), List.of())))
                .toList();

        return new EstadoCuentaResponse(
                cliente.getClienteId(),
                cliente.getNombre(),
                rango.desde(),
                rango.hasta(),
                sumar(detalle.stream().map(CuentaEstado::totalCreditos).toList()),
                sumar(detalle.stream().map(CuentaEstado::totalDebitos).toList()),
                detalle);
    }

    @Override
    public List<MovimientoReporteResponse> movimientosPlano(String clienteId, RangoFechas rango) {
        ClienteSnapshot cliente = buscarCliente(clienteId);
        return movimientoRepository.buscarParaEstadoCuenta(clienteId, rango.inicio(), rango.finExclusivo())
                .stream()
                .map(m -> new MovimientoReporteResponse(
                        m.getFecha().format(FORMATO_FECHA),
                        cliente.getNombre(),
                        m.getCuenta().getNumeroCuenta(),
                        m.getCuenta().getTipoCuenta().getDescripcion(),
                        m.getCuenta().getSaldoInicial(),
                        m.getCuenta().getEstado(),
                        m.getValor(),
                        m.getSaldo()))
                .toList();
    }

    private Map<Long, List<Movimiento>> movimientosPorCuenta(String clienteId, RangoFechas rango) {
        return movimientoRepository.buscarParaEstadoCuenta(clienteId, rango.inicio(), rango.finExclusivo())
                .stream()
                .collect(Collectors.groupingBy(m -> m.getCuenta().getId()));
    }

    private CuentaEstado construirCuentaEstado(Cuenta cuenta, List<Movimiento> movimientos) {
        List<MovimientoEstado> detalle = movimientos.stream()
                .map(m -> new MovimientoEstado(m.getId(), m.getFecha(), m.getTipoMovimiento().getDescripcion(),
                        m.getValor(), m.getSaldo()))
                .toList();

        return new CuentaEstado(
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta().getDescripcion(),
                cuenta.getSaldoInicial(),
                cuenta.getEstado(),
                cuenta.getSaldoDisponible(),
                sumarValores(movimientos, v -> v.signum() > 0),
                sumarValores(movimientos, v -> v.signum() < 0).negate(),
                detalle);
    }

    private static BigDecimal sumarValores(List<Movimiento> movimientos, Predicate<BigDecimal> filtro) {
        return movimientos.stream()
                .map(Movimiento::getValor)
                .filter(filtro)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal sumar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ClienteSnapshot buscarCliente(String clienteId) {
        return clienteSnapshotRepository.findById(clienteId)
                .orElseThrow(() -> RecursoNoEncontradoException.cliente(clienteId));
    }
}
