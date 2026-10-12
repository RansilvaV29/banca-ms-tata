package com.banca.cuentas.service.impl;

import com.banca.cuentas.domain.exception.RecursoNoEncontradoException;
import com.banca.cuentas.domain.exception.SaldoNoDisponibleException;
import com.banca.cuentas.domain.model.Cuenta;
import com.banca.cuentas.domain.model.Movimiento;
import com.banca.cuentas.domain.model.TipoCuenta;
import com.banca.cuentas.domain.model.TipoMovimiento;
import com.banca.cuentas.dto.MovimientoRequest;
import com.banca.cuentas.dto.MovimientoResponse;
import com.banca.cuentas.mapper.MovimientoMapper;
import com.banca.cuentas.repository.CuentaRepository;
import com.banca.cuentas.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceImplTest {

    private static final ZoneId ZONA = ZoneId.of("America/Guayaquil");
    private static final Clock RELOJ_FIJO = Clock.fixed(Instant.parse("2022-02-10T15:00:00Z"), ZONA);

    @Mock
    private MovimientoRepository movimientoRepository;
    @Mock
    private CuentaRepository cuentaRepository;

    private MovimientoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MovimientoServiceImpl(movimientoRepository, cuentaRepository, new MovimientoMapper(), RELOJ_FIJO);
    }

    @Test
    void registrarDepositoActualizaSaldoYUsaFechaDelServidor() {
        Cuenta cuenta = new Cuenta("225487", TipoCuenta.CORRIENTE, new BigDecimal("100"), true, "mmontalvo");
        when(cuentaRepository.bloquearPorNumeroCuenta("225487")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoResponse respuesta = service.registrar(new MovimientoRequest("225487", new BigDecimal("600")));

        assertThat(respuesta.saldo()).isEqualByComparingTo("700");
        assertThat(respuesta.tipoMovimiento()).isEqualTo(TipoMovimiento.DEPOSITO);
        assertThat(respuesta.fecha()).isEqualTo(LocalDateTime.now(RELOJ_FIJO));
        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("700");
    }

    @Test
    void registrarRetiroSinSaldoNoGuardaNada() {
        Cuenta cuenta = new Cuenta("496825", TipoCuenta.AHORROS, BigDecimal.ZERO, true, "mmontalvo");
        when(cuentaRepository.bloquearPorNumeroCuenta("496825")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> service.registrar(new MovimientoRequest("496825", new BigDecimal("-50"))))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarEnCuentaInexistenteLanzaNoEncontrado() {
        when(cuentaRepository.bloquearPorNumeroCuenta("000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(new MovimientoRequest("000000", BigDecimal.TEN)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
