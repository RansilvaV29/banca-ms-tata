package com.banca.cuentas.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cliente_snapshot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClienteSnapshot {

    @Id
    @Column(name = "cliente_id", length = 50)
    private String clienteId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado;

    /** Momento (UTC) del último evento aplicado; sirve para ignorar eventos que llegan desordenados. */
    @Column(name = "ultimo_evento", nullable = false)
    private LocalDateTime ultimoEvento;

    public ClienteSnapshot(String clienteId, String nombre, Boolean estado, LocalDateTime ultimoEvento) {
        this.clienteId = clienteId;
        actualizar(nombre, estado, ultimoEvento);
    }

    public void actualizar(String nombre, Boolean estado, LocalDateTime ultimoEvento) {
        this.nombre = nombre;
        this.estado = Boolean.TRUE.equals(estado);
        this.ultimoEvento = ultimoEvento;
    }

    public boolean esPosteriorA(LocalDateTime momentoEvento) {
        return ultimoEvento != null && ultimoEvento.isAfter(momentoEvento);
    }

    public boolean estaActivo() {
        return Boolean.TRUE.equals(estado);
    }
}
