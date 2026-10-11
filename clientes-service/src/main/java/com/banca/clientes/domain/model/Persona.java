package com.banca.clientes.domain.model;

import com.banca.clientes.domain.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Genero genero;

    private Integer edad;

    @Column(nullable = false, unique = true, length = 20)
    private String identificacion;

    @Column(length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    protected Persona(String nombre, Genero genero, Integer edad, String identificacion,
                      String direccion, String telefono) {
        this.identificacion = requerido(identificacion, "identificacion");
        actualizarDatosPersonales(nombre, genero, edad, direccion, telefono);
    }

    /**
     * Actualiza los datos personales protegiendo las invariantes del dominio.
     * La identificación no se modifica: es la identidad natural de la persona.
     */
    public void actualizarDatosPersonales(String nombre, Genero genero, Integer edad,
                                          String direccion, String telefono) {
        if (edad != null && edad < 0) {
            throw new ReglaNegocioException("EDAD_INVALIDA", "La edad no puede ser negativa");
        }
        this.nombre = requerido(nombre, "nombre");
        this.genero = genero;
        this.edad = edad;
        this.direccion = direccion;
        this.telefono = telefono;
    }

    protected static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("CAMPO_REQUERIDO", "El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }
}
