package com.banca.clientes.controller;

import com.banca.clientes.dto.ClientePatchRequest;
import com.banca.clientes.dto.ClienteRequest;
import com.banca.clientes.dto.ClienteResponse;
import com.banca.clientes.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Listar clientes")
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listar());
    }

    @Operation(summary = "Obtener un cliente por clienteId")
    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable String clienteId) {
        return ResponseEntity.ok(clienteService.obtener(clienteId));
    }

    @Operation(summary = "Crear cliente")
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{clienteId}")
                .buildAndExpand(creado.clienteId())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @Operation(summary = "Reemplazar todos los datos de un cliente")
    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable String clienteId,
                                                      @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(clienteId, request));
    }

    @Operation(summary = "Actualizar parcialmente un cliente")
    @PatchMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> actualizarParcial(@PathVariable String clienteId,
                                                             @Valid @RequestBody ClientePatchRequest request) {
        return ResponseEntity.ok(clienteService.actualizarParcial(clienteId, request));
    }

    @Operation(summary = "Eliminar cliente")
    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
        clienteService.eliminar(clienteId);
        return ResponseEntity.noContent().build();
    }
}
