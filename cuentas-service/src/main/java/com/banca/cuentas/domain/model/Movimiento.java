package com.banca.cuentas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false)
    private Cuenta cuenta;

    private Movimiento(Cuenta cuenta, BigDecimal valor, BigDecimal saldo, LocalDateTime fecha) {
        this.cuenta = cuenta;
        this.valor = valor;
        this.saldo = saldo;
        this.fecha = fecha;
        this.tipoMovimiento = TipoMovimiento.desde(valor);
    }

    /**
     * Factory method: la única forma de crear un movimiento es aplicándolo a la cuenta,
     * así es imposible tener un movimiento registrado que no haya afectado el saldo.
     */
    public static Movimiento registrar(Cuenta cuenta, BigDecimal valor, LocalDateTime fecha) {
        BigDecimal saldoResultante = cuenta.aplicarMovimiento(valor);
        return new Movimiento(cuenta, valor, saldoResultante, fecha);
    }

    /** Corrige el valor del movimiento ajustando el saldo de la cuenta. */
    public void corregirValor(BigDecimal nuevoValor) {
        cuenta.revertirMovimiento(this.valor);
        this.saldo = cuenta.aplicarMovimiento(nuevoValor);
        this.valor = nuevoValor;
        this.tipoMovimiento = TipoMovimiento.desde(nuevoValor);
    }

    /** Deshace el efecto del movimiento en la cuenta (previo a eliminarlo). */
    public void anular() {
        cuenta.revertirMovimiento(this.valor);
    }
}
