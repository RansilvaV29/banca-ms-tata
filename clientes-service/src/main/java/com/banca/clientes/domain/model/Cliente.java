package com.banca.clientes.domain.model;

import com.banca.clientes.domain.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Cliente hereda de Persona. Su PK técnica (persona_id) es la misma de persona;
 * además tiene "clienteId", una clave de negocio única que es la que se expone en la API
 * y la que usa el microservicio de cuentas para referenciarlo (sin acoplarse a ids internos).
 */
@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cliente extends Persona {

    @Column(name = "cliente_id", nullable = false, unique = true, length = 50)
    private String clienteId;

    /** Siempre se almacena cifrada (BCrypt). Nunca se expone en las respuestas. */
    @Column(nullable = false, length = 100)
    private String contrasena;

    @Column(nullable = false)
    private Boolean estado;

    private Cliente(Builder b) {
        super(b.nombre, b.genero, b.edad, b.identificacion, b.direccion, b.telefono);
        this.clienteId = requerido(b.clienteId, "clienteId");
        this.contrasena = requerido(b.contrasenaCifrada, "contrasena");
        this.estado = b.estado == null ? Boolean.TRUE : b.estado;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void cambiarContrasena(String contrasenaCifrada) {
        this.contrasena = requerido(contrasenaCifrada, "contrasena");
    }

    public void cambiarEstado(Boolean nuevoEstado) {
        if (nuevoEstado == null) {
            throw new ReglaNegocioException("CAMPO_REQUERIDO", "El campo 'estado' es obligatorio");
        }
        this.estado = nuevoEstado;
    }

    public void activar() {
        this.estado = Boolean.TRUE;
    }

    public void desactivar() {
        this.estado = Boolean.FALSE;
    }

    public boolean estaActivo() {
        return Boolean.TRUE.equals(estado);
    }


    public static final class Builder {
        private String clienteId;
        private String nombre;
        private Genero genero;
        private Integer edad;
        private String identificacion;
        private String direccion;
        private String telefono;
        private String contrasenaCifrada;
        private Boolean estado;

        private Builder() {
        }

        public Builder clienteId(String clienteId) { this.clienteId = clienteId; return this; }
        public Builder nombre(String nombre) { this.nombre = nombre; return this; }
        public Builder genero(Genero genero) { this.genero = genero; return this; }
        public Builder edad(Integer edad) { this.edad = edad; return this; }
        public Builder identificacion(String identificacion) { this.identificacion = identificacion; return this; }
        public Builder direccion(String direccion) { this.direccion = direccion; return this; }
        public Builder telefono(String telefono) { this.telefono = telefono; return this; }
        public Builder contrasenaCifrada(String contrasenaCifrada) { this.contrasenaCifrada = contrasenaCifrada; return this; }
        public Builder estado(Boolean estado) { this.estado = estado; return this; }

        public Cliente build() {
            return new Cliente(this);
        }
    }
}
