package com.banca.cuentas.service.impl;

import com.banca.cuentas.domain.exception.ConflictoException;
import com.banca.cuentas.domain.exception.RecursoNoEncontradoException;
import com.banca.cuentas.domain.exception.ReglaNegocioException;
import com.banca.cuentas.domain.model.ClienteSnapshot;
import com.banca.cuentas.domain.model.Cuenta;
import com.banca.cuentas.domain.model.TipoCuenta;
import com.banca.cuentas.dto.CuentaPatchRequest;
import com.banca.cuentas.dto.CuentaRequest;
import com.banca.cuentas.dto.CuentaResponse;
import com.banca.cuentas.dto.CuentaUpdateRequest;
import com.banca.cuentas.mapper.CuentaMapper;
import com.banca.cuentas.repository.ClienteSnapshotRepository;
import com.banca.cuentas.repository.CuentaRepository;
import com.banca.cuentas.repository.MovimientoRepository;
import com.banca.cuentas.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClienteSnapshotRepository clienteSnapshotRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    public List<CuentaResponse> listar(String clienteId) {
        List<Cuenta> cuentas = (clienteId == null || clienteId.isBlank())
                ? cuentaRepository.findAll()
                : cuentaRepository.findByClienteIdOrderByNumeroCuenta(clienteId);
        return cuentas.stream().map(cuentaMapper::toResponse).toList();
    }

    @Override
    public CuentaResponse obtener(String numeroCuenta) {
        return cuentaMapper.toResponse(buscar(numeroCuenta));
    }

    @Override
    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        if (cuentaRepository.existsByNumeroCuenta(request.numeroCuenta())) {
            throw new ConflictoException("CUENTA_DUPLICADA",
                    "Ya existe la cuenta '" + request.numeroCuenta() + "'");
        }
        ClienteSnapshot cliente = clienteSnapshotRepository.findById(request.clienteId())
                .orElseThrow(() -> RecursoNoEncontradoException.cliente(request.clienteId()));
        if (!cliente.estaActivo()) {
            throw new ReglaNegocioException("CLIENTE_INACTIVO",
                    "El cliente '" + request.clienteId() + "' está inactivo y no puede abrir cuentas");
        }

        Cuenta cuenta = new Cuenta(request.numeroCuenta(), request.tipoCuenta(), request.saldoInicial(),
                request.estado(), cliente.getClienteId());
        return cuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public CuentaResponse actualizar(String numeroCuenta, CuentaUpdateRequest request) {
        Cuenta cuenta = buscar(numeroCuenta);
        aplicarCambios(cuenta, request.tipoCuenta(), request.saldoInicial(), request.estado());
        return cuentaMapper.toResponse(cuenta);
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(String numeroCuenta, CuentaPatchRequest request) {
        Cuenta cuenta = buscar(numeroCuenta);
        aplicarCambios(cuenta, request.tipoCuenta(), request.saldoInicial(), request.estado());
        return cuentaMapper.toResponse(cuenta);
    }

    @Override
    @Transactional
    public void eliminar(String numeroCuenta) {
        Cuenta cuenta = buscar(numeroCuenta);
        if (movimientoRepository.existsByCuentaId(cuenta.getId())) {
            throw new ConflictoException("CUENTA_CON_MOVIMIENTOS",
                    "La cuenta tiene movimientos registrados y no puede eliminarse; desactívela con PATCH {\"estado\": false}");
        }
        cuentaRepository.delete(cuenta);
    }

    private void aplicarCambios(Cuenta cuenta, TipoCuenta tipo, BigDecimal saldoInicial, Boolean estado) {
        Optional.ofNullable(tipo).ifPresent(cuenta::cambiarTipo);
        Optional.ofNullable(estado).ifPresent(cuenta::cambiarEstado);

        if (saldoInicial != null && saldoInicial.compareTo(cuenta.getSaldoInicial()) != 0) {
            if (movimientoRepository.existsByCuentaId(cuenta.getId())) {
                throw new ReglaNegocioException("SALDO_INICIAL_INMUTABLE",
                        "No se puede modificar el saldo inicial de una cuenta que ya tiene movimientos");
            }
            cuenta.redefinirSaldoInicial(saldoInicial);
        }
    }

    private Cuenta buscar(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> RecursoNoEncontradoException.cuenta(numeroCuenta));
    }
}
