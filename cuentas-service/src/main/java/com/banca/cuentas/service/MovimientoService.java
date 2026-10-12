package com.banca.cuentas.service;

import com.banca.cuentas.dto.MovimientoRequest;
import com.banca.cuentas.dto.MovimientoResponse;
import com.banca.cuentas.dto.MovimientoUpdateRequest;
import com.banca.cuentas.dto.PaginaResponse;
import org.springframework.data.domain.Pageable;

public interface MovimientoService {

    PaginaResponse<MovimientoResponse> listar(String numeroCuenta, Pageable pageable);

    MovimientoResponse obtener(Long id);

    MovimientoResponse registrar(MovimientoRequest request);

    MovimientoResponse actualizar(Long id, MovimientoUpdateRequest request);

    void eliminar(Long id);
}
