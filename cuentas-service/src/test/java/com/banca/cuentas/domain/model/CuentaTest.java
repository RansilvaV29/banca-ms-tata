package com.banca.cuentas.domain.model;

import com.banca.cuentas.domain.exception.ReglaNegocioException;
import com.banca.cuentas.domain.exception.SaldoNoDisponibleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private Cuenta cuenta(String saldo) {
        return new Cuenta("478758", TipoCuenta.AHORROS, new BigDecimal(saldo), true, "jlema");
    }

    @ParameterizedTest(name = "saldo {0} + movimiento {1} = {2}")
    @CsvSource({
            "2000, -575, 1425",   // caso de uso: retiro de 575
            "100,   600,  700",   // caso de uso: depósito de 600
            "540,  -540,    0"    /* retirar exactamente todo el saldo está permitido*/
    })
    void aplicarMovimientoActualizaSaldo(String saldo, String valor, String esperado) {
        Cuenta cuenta = cuenta(saldo);

        BigDecimal nuevoSaldo = cuenta.aplicarMovimiento(new BigDecimal(valor));

        assertThat(nuevoSaldo).isEqualByComparingTo(esperado);
        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo(esperado);
    }

    @Test
    @DisplayName("F3: un retiro mayor al saldo lanza 'Saldo no disponible' y no altera el saldo")
    void retiroSinSaldoLanzaExcepcion() {
        Cuenta cuenta = cuenta("0");

        assertThatThrownBy(() -> cuenta.aplicarMovimiento(new BigDecimal("-1")))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("0");
    }

    @Test
    void cuentaInactivaNoAdmiteMovimientos() {
        Cuenta cuenta = cuenta("100");
        cuenta.desactivar();

        assertThatThrownBy(() -> cuenta.aplicarMovimiento(BigDecimal.TEN))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void movimientoDeValorCeroNoEsValido() {
        assertThatThrownBy(() -> cuenta("100").aplicarMovimiento(BigDecimal.ZERO))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void registrarMovimientoGuardaTipoYSaldoResultante() {
        Cuenta cuenta = cuenta("2000");

        Movimiento retiro = Movimiento.registrar(cuenta, new BigDecimal("-575"), LocalDateTime.now());

        assertThat(retiro.getTipoMovimiento()).isEqualTo(TipoMovimiento.RETIRO);
        assertThat(retiro.getSaldo()).isEqualByComparingTo("1425");
    }

    @Test
    void anularMovimientoRestauraElSaldo() {
        Cuenta cuenta = cuenta("100");
        Movimiento deposito = Movimiento.registrar(cuenta, new BigDecimal("600"), LocalDateTime.now());

        deposito.anular();

        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("100");
    }
}
