package com.banca.cuentas.controller;

import com.banca.cuentas.dto.MovimientoRequest;
import com.banca.cuentas.dto.MovimientoResponse;
import com.banca.cuentas.dto.MovimientoUpdateRequest;
import com.banca.cuentas.dto.PaginaResponse;
import com.banca.cuentas.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Movimientos")
@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @Operation(summary = "Listar movimientos paginados (opcionalmente por número de cuenta)")
    @GetMapping
    public ResponseEntity<PaginaResponse<MovimientoResponse>> listar(
            @RequestParam(required = false) String numeroCuenta,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(movimientoService.listar(numeroCuenta, pageable));
    }

    @Operation(summary = "Obtener un movimiento")
    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtener(id));
    }

    @Operation(summary = "Registrar movimiento: valor positivo = depósito, negativo = retiro")
    @PostMapping
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        MovimientoResponse registrado = movimientoService.registrar(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registrado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(registrado);
    }

    @Operation(summary = "Corregir el valor del último movimiento de una cuenta")
    @PutMapping("/{id}")
    public ResponseEntity<MovimientoResponse> actualizar(@PathVariable Long id,
                                                         @Valid @RequestBody MovimientoUpdateRequest request) {
        return ResponseEntity.ok(movimientoService.actualizar(id, request));
    }

    @Operation(summary = "Eliminar (anular) el último movimiento de una cuenta")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
