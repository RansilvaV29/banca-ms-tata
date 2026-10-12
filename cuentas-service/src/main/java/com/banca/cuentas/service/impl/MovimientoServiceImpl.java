package com.banca.cuentas.service.impl;

import com.banca.cuentas.domain.exception.RecursoNoEncontradoException;
import com.banca.cuentas.domain.exception.ReglaNegocioException;
import com.banca.cuentas.domain.model.Cuenta;
import com.banca.cuentas.domain.model.Movimiento;
import com.banca.cuentas.dto.MovimientoRequest;
import com.banca.cuentas.dto.MovimientoResponse;
import com.banca.cuentas.dto.MovimientoUpdateRequest;
import com.banca.cuentas.dto.PaginaResponse;
import com.banca.cuentas.mapper.MovimientoMapper;
import com.banca.cuentas.repository.CuentaRepository;
import com.banca.cuentas.repository.MovimientoRepository;
import com.banca.cuentas.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoMapper movimientoMapper;
    private final Clock clock; // inyectado para poder fijar la hora en las pruebas

    @Override
    public PaginaResponse<MovimientoResponse> listar(String numeroCuenta, Pageable pageable) {
        Page<Movimiento> pagina = (numeroCuenta == null || numeroCuenta.isBlank())
                ? movimientoRepository.findAll(pageable)
                : movimientoRepository.findByCuentaNumeroCuenta(numeroCuenta, pageable);
        return PaginaResponse.de(pagina, movimientoMapper::toResponse);
    }

    @Override
    public MovimientoResponse obtener(Long id) {
        return movimientoRepository.findWithCuentaById(id)
                .map(movimientoMapper::toResponse)
                .orElseThrow(() -> RecursoNoEncontradoException.movimiento(id));
    }

    @Override
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        Cuenta cuenta = cuentaRepository.bloquearPorNumeroCuenta(request.numeroCuenta())
                .orElseThrow(() -> RecursoNoEncontradoException.cuenta(request.numeroCuenta()));

        Movimiento movimiento = Movimiento.registrar(cuenta, request.valor(), LocalDateTime.now(clock));
        Movimiento guardado = movimientoRepository.save(movimiento);

        log.info("Movimiento {} registrado en cuenta {}: valor={}, saldo={}",
                guardado.getId(), cuenta.getNumeroCuenta(), guardado.getValor(), guardado.getSaldo());
        return movimientoMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public MovimientoResponse actualizar(Long id, MovimientoUpdateRequest request) {
        Cuenta cuenta = bloquearCuentaDelMovimiento(id);
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.movimiento(id));
        validarEsUltimoMovimiento(movimiento, cuenta);

        movimiento.corregirValor(request.valor());
        return movimientoMapper.toResponse(movimiento);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cuenta cuenta = bloquearCuentaDelMovimiento(id);
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.movimiento(id));
        validarEsUltimoMovimiento(movimiento, cuenta);

        movimiento.anular();
        movimientoRepository.delete(movimiento);
    }

    private Cuenta bloquearCuentaDelMovimiento(Long movimientoId) {
        Long cuentaId = movimientoRepository.buscarIdCuenta(movimientoId)
                .orElseThrow(() -> RecursoNoEncontradoException.movimiento(movimientoId));
        return cuentaRepository.bloquearPorId(cuentaId)
                .orElseThrow(() -> RecursoNoEncontradoException.movimiento(movimientoId));
    }

    private void validarEsUltimoMovimiento(Movimiento movimiento, Cuenta cuenta) {
        Long ultimoId = movimientoRepository.findFirstByCuentaIdOrderByIdDesc(cuenta.getId())
                .map(Movimiento::getId)
                .orElse(null);
        if (!movimiento.getId().equals(ultimoId)) {
            throw new ReglaNegocioException("MOVIMIENTO_NO_MODIFICABLE",
                    "Solo se puede modificar o eliminar el último movimiento de la cuenta");
        }
    }
}
