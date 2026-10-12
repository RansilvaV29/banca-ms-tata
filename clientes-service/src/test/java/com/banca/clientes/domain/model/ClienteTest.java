package com.banca.clientes.domain.model;

import com.banca.clientes.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** F5: prueba unitaria de la entidad de dominio Cliente (sin Spring, sin base de datos). */
class ClienteTest {

    private Cliente.Builder clienteValido() {
        return Cliente.builder()
                .clienteId("jlema")
                .nombre("Jose Lema")
                .genero(Genero.MASCULINO)
                .edad(35)
                .identificacion("1712345678")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .contrasenaCifrada("$2a$10$hash");
    }

    @Test
    @DisplayName("Un cliente nuevo es una Persona y queda activo por defecto")
    void clienteNuevoHeredaDePersonaYEstaActivo() {
        Cliente cliente = clienteValido().build();

        assertThat(cliente).isInstanceOf(Persona.class);
        assertThat(cliente.getNombre()).isEqualTo("Jose Lema");
        assertThat(cliente.getIdentificacion()).isEqualTo("1712345678");
        assertThat(cliente.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("Se puede desactivar y volver a activar un cliente")
    void desactivarYActivar() {
        Cliente cliente = clienteValido().build();

        cliente.desactivar();
        assertThat(cliente.estaActivo()).isFalse();

        cliente.activar();
        assertThat(cliente.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("No se permite crear un cliente sin nombre")
    void nombreObligatorio() {
        assertThatThrownBy(() -> clienteValido().nombre("  ").build())
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    @DisplayName("La edad no puede ser negativa")
    void edadNegativaNoPermitida() {
        Cliente cliente = clienteValido().build();

        assertThatThrownBy(() -> cliente.actualizarDatosPersonales("Jose Lema", Genero.MASCULINO, -1,
                "Otavalo", "098254785"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La edad no puede ser negativa");
    }
}
