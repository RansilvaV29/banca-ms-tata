package com.banca.cuentas.service;

import com.banca.cuentas.dto.CuentaPatchRequest;
import com.banca.cuentas.dto.CuentaRequest;
import com.banca.cuentas.dto.CuentaResponse;
import com.banca.cuentas.dto.CuentaUpdateRequest;

import java.util.List;

public interface CuentaService {

    List<CuentaResponse> listar(String clienteId);

    CuentaResponse obtener(String numeroCuenta);

    CuentaResponse crear(CuentaRequest request);

    CuentaResponse actualizar(String numeroCuenta, CuentaUpdateRequest request);

    CuentaResponse actualizarParcial(String numeroCuenta, CuentaPatchRequest request);

    void eliminar(String numeroCuenta);
}
