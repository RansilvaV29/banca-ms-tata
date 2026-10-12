package com.banca.cuentas.domain.model;

import com.banca.cuentas.domain.exception.ReglaNegocioException;
import com.banca.cuentas.domain.exception.SaldoNoDisponibleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "cuenta")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 20)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_disponible", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(nullable = false)
    private Boolean estado;

    @Column(name = "cliente_id", nullable = false, length = 50)
    private String clienteId;

    /** Bloqueo optimista: detecta modificaciones concurrentes de la misma cuenta. */
    @Version
    private Long version;

    public Cuenta(String numeroCuenta, TipoCuenta tipoCuenta, BigDecimal saldoInicial,
                  Boolean estado, String clienteId) {
        this.numeroCuenta = Objects.requireNonNull(numeroCuenta, "numeroCuenta");
        this.tipoCuenta = Objects.requireNonNull(tipoCuenta, "tipoCuenta");
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.estado = estado == null ? Boolean.TRUE : estado;
        redefinirSaldoInicial(saldoInicial);
    }


    public BigDecimal aplicarMovimiento(BigDecimal valor) {
        validarActiva();
        validarValor(valor);
        BigDecimal nuevoSaldo = saldoDisponible.add(valor);
        if (nuevoSaldo.signum() < 0) {
            throw new SaldoNoDisponibleException();
        }
        this.saldoDisponible = nuevoSaldo;
        return nuevoSaldo;
    }

    public void revertirMovimiento(BigDecimal valor) {
        BigDecimal nuevoSaldo = saldoDisponible.subtract(valor);
        if (nuevoSaldo.signum() < 0) {
            throw new SaldoNoDisponibleException();
        }
        this.saldoDisponible = nuevoSaldo;
    }

    public void redefinirSaldoInicial(BigDecimal nuevoSaldoInicial) {
        if (nuevoSaldoInicial == null || nuevoSaldoInicial.signum() < 0) {
            throw new ReglaNegocioException("SALDO_INICIAL_INVALIDO", "El saldo inicial no puede ser negativo");
        }
        this.saldoInicial = nuevoSaldoInicial;
        this.saldoDisponible = nuevoSaldoInicial;
    }

    public void cambiarTipo(TipoCuenta nuevoTipo) {
        this.tipoCuenta = Objects.requireNonNull(nuevoTipo, "tipoCuenta");
    }

    public void cambiarEstado(Boolean nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "estado");
    }

    public void desactivar() {
        this.estado = Boolean.FALSE;
    }

    public boolean estaActiva() {
        return Boolean.TRUE.equals(estado);
    }

    private void validarActiva() {
        if (!estaActiva()) {
            throw new ReglaNegocioException("CUENTA_INACTIVA",
                    "La cuenta " + numeroCuenta + " está inactiva y no admite movimientos");
        }
    }

    private static void validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() == 0) {
            throw new ReglaNegocioException("VALOR_INVALIDO", "El valor del movimiento debe ser distinto de cero");
        }
    }
}
