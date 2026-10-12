package com.banca.cuentas.controller;

import com.banca.cuentas.dto.CuentaPatchRequest;
import com.banca.cuentas.dto.CuentaRequest;
import com.banca.cuentas.dto.CuentaResponse;
import com.banca.cuentas.dto.CuentaUpdateRequest;
import com.banca.cuentas.service.CuentaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Cuentas")
@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @Operation(summary = "Listar cuentas (opcionalmente filtradas por clienteId)")
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar(@RequestParam(required = false) String clienteId) {
        return ResponseEntity.ok(cuentaService.listar(clienteId));
    }

    @Operation(summary = "Obtener una cuenta por número")
    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> obtener(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtener(numeroCuenta));
    }

    @Operation(summary = "Crear cuenta para un cliente existente")
    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        CuentaResponse creada = cuentaService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numeroCuenta}")
                .buildAndExpand(creada.numeroCuenta())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @Operation(summary = "Actualizar tipo, saldo inicial (si no tiene movimientos) y estado")
    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> actualizar(@PathVariable String numeroCuenta,
                                                     @Valid @RequestBody CuentaUpdateRequest request) {
        return ResponseEntity.ok(cuentaService.actualizar(numeroCuenta, request));
    }

    @Operation(summary = "Actualizar parcialmente una cuenta (p. ej. desactivarla)")
    @PatchMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> actualizarParcial(@PathVariable String numeroCuenta,
                                                            @Valid @RequestBody CuentaPatchRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(numeroCuenta, request));
    }

    @Operation(summary = "Eliminar una cuenta sin movimientos")
    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroCuenta) {
        cuentaService.eliminar(numeroCuenta);
        return ResponseEntity.noContent().build();
    }
}
