package com.banca.clientes.service.impl;

import com.banca.clientes.domain.exception.RecursoDuplicadoException;
import com.banca.clientes.domain.exception.RecursoNoEncontradoException;
import com.banca.clientes.domain.exception.ReglaNegocioException;
import com.banca.clientes.domain.model.Cliente;
import com.banca.clientes.dto.ClientePatchRequest;
import com.banca.clientes.dto.ClienteRequest;
import com.banca.clientes.dto.ClienteResponse;
import com.banca.clientes.mapper.ClienteMapper;
import com.banca.clientes.repository.ClienteRepository;
import com.banca.clientes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // por defecto solo lectura; los métodos que escriben lo sobrescriben
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Override
    public ClienteResponse obtener(String clienteId) {
        return clienteMapper.toResponse(buscar(clienteId));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        String clienteId = Optional.ofNullable(request.clienteId())
                .filter(id -> !id.isBlank())
                .orElseGet(this::generarClienteId);

        if (clienteRepository.existsByClienteId(clienteId)) {
            throw new RecursoDuplicadoException("CLIENTE_DUPLICADO",
                    "Ya existe un cliente con clienteId '" + clienteId + "'");
        }
        validarIdentificacionDisponible(request.identificacion());

        Cliente cliente = Cliente.builder()
                .clienteId(clienteId)
                .nombre(request.nombre())
                .genero(request.genero())
                .edad(request.edad())
                .identificacion(request.identificacion())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .contrasenaCifrada(passwordEncoder.encode(request.contrasena()))
                .estado(request.estado())
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(guardado);
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(String clienteId, ClienteRequest request) {
        Cliente cliente = buscar(clienteId);

        if (!cliente.getIdentificacion().equals(request.identificacion())) {
            throw new ReglaNegocioException("IDENTIFICACION_INMUTABLE",
                    "La identificación de un cliente no puede modificarse");
        }

        cliente.actualizarDatosPersonales(request.nombre(), request.genero(), request.edad(),
                request.direccion(), request.telefono());
        cliente.cambiarContrasena(passwordEncoder.encode(request.contrasena()));
        cliente.cambiarEstado(Optional.ofNullable(request.estado()).orElse(cliente.getEstado()));

        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(String clienteId, ClientePatchRequest request) {
        Cliente cliente = buscar(clienteId);

        cliente.actualizarDatosPersonales(
                valorOActual(request.nombre(), cliente.getNombre()),
                valorOActual(request.genero(), cliente.getGenero()),
                valorOActual(request.edad(), cliente.getEdad()),
                valorOActual(request.direccion(), cliente.getDireccion()),
                valorOActual(request.telefono(), cliente.getTelefono()));

        Optional.ofNullable(request.contrasena())
                .map(passwordEncoder::encode)
                .ifPresent(cliente::cambiarContrasena);
        Optional.ofNullable(request.estado()).ifPresent(cliente::cambiarEstado);

        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void eliminar(String clienteId) {
        Cliente cliente = buscar(clienteId);
        clienteRepository.delete(cliente);
    }

    private Cliente buscar(String clienteId) {
        return clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> RecursoNoEncontradoException.cliente(clienteId));
    }

    private void validarIdentificacionDisponible(String identificacion) {
        if (clienteRepository.existsByIdentificacion(identificacion)) {
            throw new RecursoDuplicadoException("IDENTIFICACION_DUPLICADA",
                    "Ya existe un cliente con identificación '" + identificacion + "'");
        }
    }

    private String generarClienteId() {
        return "CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static <T> T valorOActual(T nuevo, T actual) {
        return nuevo != null ? nuevo : actual;
    }
}
