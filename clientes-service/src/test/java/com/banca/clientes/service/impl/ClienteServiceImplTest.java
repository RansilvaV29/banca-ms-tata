package com.banca.clientes.service.impl;

import com.banca.clientes.domain.exception.RecursoDuplicadoException;
import com.banca.clientes.domain.exception.RecursoNoEncontradoException;
import com.banca.clientes.domain.model.Cliente;
import com.banca.clientes.domain.model.Genero;
import com.banca.clientes.dto.ClienteRequest;
import com.banca.clientes.dto.ClienteResponse;
import com.banca.clientes.event.ClienteEvent;
import com.banca.clientes.event.TipoEventoCliente;
import com.banca.clientes.mapper.ClienteMapper;
import com.banca.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ClienteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClienteServiceImpl(clienteRepository, new ClienteMapper(), passwordEncoder, eventPublisher);
    }

    private ClienteRequest request() {
        return new ClienteRequest("jlema", "Jose Lema", Genero.MASCULINO, 35, "1712345678",
                "Otavalo sn y principal", "098254785", "1234", true);
    }

    @Test
    void crearCifraContrasenaYPublicaEvento() {
        when(clienteRepository.existsByClienteId("jlema")).thenReturn(false);
        when(clienteRepository.existsByIdentificacion("1712345678")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hash-1234");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse respuesta = service.crear(request());

        ArgumentCaptor<Cliente> guardado = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(guardado.capture());
        assertThat(guardado.getValue().getContrasena()).isEqualTo("hash-1234");
        assertThat(respuesta.clienteId()).isEqualTo("jlema");

        ArgumentCaptor<ClienteEvent> evento = ArgumentCaptor.forClass(ClienteEvent.class);
        verify(eventPublisher).publishEvent(evento.capture());
        assertThat(evento.getValue().tipo()).isEqualTo(TipoEventoCliente.CLIENTE_CREADO);
        assertThat(evento.getValue().clienteId()).isEqualTo("jlema");
    }

    @Test
    void crearConIdentificacionDuplicadaLanzaConflicto() {
        when(clienteRepository.existsByClienteId("jlema")).thenReturn(false);
        when(clienteRepository.existsByIdentificacion("1712345678")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(RecursoDuplicadoException.class);
        verify(clienteRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void obtenerClienteInexistenteLanzaNoEncontrado() {
        when(clienteRepository.findByClienteId("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener("nadie"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("nadie");
    }
}
